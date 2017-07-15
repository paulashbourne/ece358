package com.ece358;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class SingleClientServer implements Runnable {
  private Integer ackNumber;
  private Integer myPort;
  private ServerState state = ServerState.CLOSED;
  private BlockingQueue<Packet> packets;
  private String clientIpAddress;
  private int clientPort;
  private String filePath;
  DatagramSocket datagramSocket;
  private Integer sequenceNumber;
  private FileInputStream fileInputStream;

  public SingleClientServer(String clientIpAddress, int clientPort, String filePath)
      throws SocketException {
    this.clientIpAddress = clientIpAddress;
    this.clientPort = clientPort;
    this.filePath = filePath;
    this.packets = new LinkedBlockingDeque<>();
    this.datagramSocket = new DatagramSocket();
    this.sequenceNumber = 0;
    this.myPort = null;
    this.ackNumber = 0;
  }

  synchronized boolean addPacket(Packet packet) {
    return packets.add(packet);
  }

  public ServerState getState() {
    return state;
  }

  @Override public void run() {
    while (true) {
      Packet packet;
      try {
        packet = packets.take();
        if (packet.FIN) {
          return;
        }
        if (packet.sequenceNumber + 1 > ackNumber) {
          ackNumber = packet.sequenceNumber + 1;
        }
      } catch (InterruptedException e) {
        continue;
      }

      switch (state) {
        case CLOSED:
          if (packet.SYN) {
            this.myPort = packet.destinationPort;
            Packet responsePacket = new Packet.Builder()
                .SYN(true)
                .ACK(true)
                .sourcePort(myPort)
                .destinationPort(clientPort)
                .sequenceNumber(sequenceNumber)
                .ackNumber(ackNumber)
                .build();
            sequenceNumber += 1;
            try {
              DatagramPacket responseDatagramPacket = Utils.packetToDatagramPacket(responsePacket,
                  clientIpAddress, clientPort);
              datagramSocket.send(responseDatagramPacket);
              state = ServerState.SYN_RECD;
            } catch (java.io.IOException ignored) {
            }
          }
          break;
        case SYN_RECD:
          if (packet.ACK) {
            state = ServerState.ESTAB;
            String fileName = String.format("%s.%s.%s.%s",
                clientIpAddress, clientPort,
                // TODO(jgulbronson) - Get actual address
                "localhost", myPort);
            Packet responsePacket;
            try {
              // TODO(jgulbronson) - Do we do this all in one packet?
              String fullFilePath = filePath + "/" + fileName;
              File file = new File(fullFilePath);
              fileInputStream = new FileInputStream(file);
              long fileLength = file.length();
              ByteBuffer fileDataBuffer = ByteBuffer.allocate((int) fileLength + 4);
              fileDataBuffer.put(longToBytes(fileLength));
              byte[] buffer = new byte[255];
              int numRead;
              while ((numRead = fileInputStream.read(buffer)) >= 0) {
                fileDataBuffer.put(buffer, 0, numRead);
              }
              responsePacket = new Packet.Builder()
                  .sourcePort(myPort)
                  .destinationPort(clientPort)
                  .ackNumber(ackNumber)
                  .sequenceNumber(sequenceNumber + 4 + (int) fileLength)
                  .segmentSize(24 + (int) fileLength)
                  .payload(fileDataBuffer.array())
                  .build();
              sequenceNumber += 4 + (int) fileLength;
            } catch (IOException e) {
              // TODO(jgulbronson) - initiate connection close
              continue;
            }
            try {
              DatagramPacket responseDatagramPacket = Utils.packetToDatagramPacket(responsePacket,
                  clientIpAddress, clientPort);
              datagramSocket.send(responseDatagramPacket);
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
          break;
        case ESTAB:
          System.out.println("Client wants more data!");
      }
    }
  }

  public byte[] longToBytes(long fileSize) {
    ByteBuffer buffer = ByteBuffer.allocate(4);
    buffer.putInt((int) fileSize);
    return buffer.array();
  }

  public void shutdown() {
    packets.add(new Packet.Builder().FIN(true).build());
  }
}
