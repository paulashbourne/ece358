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
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleClientServer implements Runnable {
  Integer ackNumber;
  Integer myPort;
  ServerState state = ServerState.CLOSED;
  BlockingQueue<Packet> packets;
  String clientIpAddress;
  int clientPort;
  String filePath;
  DatagramSocket datagramSocket;
  Integer sequenceNumber;
  FileInputStream fileInputStream;
  Logger logger;
  Packet.Builder lastSentPacketBuilder;

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
    this.logger = Logger.getLogger(
        SingleClientServer.class.getName() + "-" + this.clientIpAddress + ":" + this.clientPort);
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
        packet = packets.poll(100, TimeUnit.MILLISECONDS);
        // If we don't
        if (packet == null) {
          sendPacket(lastSentPacketBuilder);
          continue;
        }
        if (packet.FIN) {
          return;
        }
        // TODO - check if it's an old packet. If so, discard and take another.
        // TODO - Updating the ack number should go at the bottom of the loop.
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
            Packet.Builder responsePacketBuilder = defaultPacketBuilder()
                .SYN(true)
                .ACK(true)
                .ackNumber(ackNumber);
            if (sendPacket(responsePacketBuilder)) {
              state = ServerState.SYN_RECD;
            }
          }
          break;
        case SYN_RECD:
          if (packet.ACK) {
            String fileName = String.format("%s.%s.%s.%s",
                clientIpAddress, clientPort,
                // TODO(jgulbronson) - Get actual address
                "localhost", myPort);
            Packet.Builder responsePacketBuilder;
            try {
              // TODO(jgulbronson) - Split up in to multiple packets.
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
              responsePacketBuilder = defaultPacketBuilder()
                  .ackNumber(ackNumber)
                  .payload(fileDataBuffer.array());
              sequenceNumber += 4 + (int) fileLength;
            } catch (IOException e) {
              // TODO(jgulbronson) - initiate connection close
              logger.log(Level.SEVERE, "Can't find file", e);
              continue;
            }
            if (sendPacket(responsePacketBuilder)) {
              state = ServerState.ESTAB;
            }
          }
          break;
        case ESTAB:
          System.out.println("Client wants more data!");
      }
    }
  }

  // Returns true if the packet is sent successfully, otherwise false
  private boolean sendPacket(Packet.Builder packetBuilder) {
    try {
      if (packetBuilder.payload == null) {
        if (packetBuilder.FIN || packetBuilder.SYN) {
          sequenceNumber += 1;
        }
      } else {
        sequenceNumber += packetBuilder.payload.length;
      }
      int payloadLength;
      if (packetBuilder.payload == null) {
        payloadLength = 0;
      } else {
        payloadLength = packetBuilder.payload.length;
      }
      Packet packet = packetBuilder
          .segmentSize(20 + payloadLength)
          .sequenceNumber(sequenceNumber)
          .build();
      DatagramPacket responseDatagramPacket =
          Utils.packetToDatagramPacket(packet, clientIpAddress, clientPort);
      datagramSocket.send(responseDatagramPacket);
      lastSentPacketBuilder = packetBuilder;
      return true;
    } catch (java.io.IOException ignored) {
      return false;
    }
  }

  private Packet.Builder defaultPacketBuilder() {
    return new Packet.Builder()
        .sourcePort(myPort)
        .destinationPort(clientPort);
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
