package com.ece358;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class Client {
  private Integer clientPort;
  private Integer serverPort;
  private Integer sequenceNumber = 0;

  public Client(Integer clientPort, Integer serverPort) {
    this.clientPort = clientPort;
    this.serverPort = serverPort;
  }

  public void start() throws IOException {
    DatagramPacket sendPacket;
    DatagramSocket socket = new DatagramSocket(clientPort);
    byte[] buffer = new byte[256];
    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
    Packet packet = new Packet.Builder()
        .SYN(true)
        .destinationPort(serverPort)
        .sourcePort(socket.getLocalPort())
        .sequenceNumber(sequenceNumber)
        .build();
    sequenceNumber += 1;
    sendPacket = Utils.packetToDatagramPacket(packet, "localhost", serverPort);
    socket.send(sendPacket);
    socket.receive(datagramPacket);
    packet = Packet.fromDatagram(datagramPacket);
    System.out.println("Packet received");
    if (packet.hasErrors()) {
      throw new RuntimeException("Packet has errors");
    }
    if (!(packet.ACK && packet.SYN)) {
      throw new RuntimeException("Expected SYN-ACK");
    }
    if (packet.ackNumber != 1) {
      throw new RuntimeException("Expected ack number to be 1");
    }

    packet = new Packet.Builder()
        .ACK(true)
        .destinationPort(serverPort)
        .sourcePort(socket.getLocalPort())
        .sequenceNumber(sequenceNumber)
        .build();
    sendPacket = Utils.packetToDatagramPacket(packet, "localhost", serverPort);
    socket.send(sendPacket);
    socket.receive(datagramPacket);

    packet = Packet.fromDatagram(datagramPacket);
    System.out.println("Packet received");
    if (packet.hasErrors()) {
      throw new RuntimeException("Packet has errors");
    }
    if (packet.payload.length == 0) {
      throw new RuntimeException("Payload should not be empty");
    }
    if (packet.ackNumber != 2) {
      throw new RuntimeException("Expected ack number to be 2");
    }

    byte[] fileData = Arrays.copyOfRange(packet.payload, 4, packet.payload.length);
    System.out.println("Received");
    System.out.println(new String(fileData));

    packet = new Packet.Builder()
        .FIN(true)
        .destinationPort(serverPort)
        .sourcePort(socket.getLocalPort())
        .build();
    sendPacket = Utils.packetToDatagramPacket(packet, "localhost", serverPort);
    socket.send(sendPacket);
    socket.receive(datagramPacket);

    packet = Packet.fromDatagram(datagramPacket);
    System.out.println("FIN Packet received");

    if (packet.hasErrors()) {
      throw new RuntimeException("Packet has errors");
    }
    if (packet.payload.length != 0) {
      throw new RuntimeException("Payload should be empty");
    }
    if (!packet.ACK) {
      throw new RuntimeException("ACL should be true");
    }
  }

  public static void main(String[] args) throws IOException {
    Integer clientPort = Integer.parseInt(args[0]);
    Integer serverPort = Integer.parseInt(args[1]);

    Client client = new Client(clientPort, serverPort);
    client.start();
  }
}
