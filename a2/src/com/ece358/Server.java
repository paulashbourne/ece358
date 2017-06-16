package com.ece358;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Server {
  private Integer portNum;

  public Server(Integer portNum) {
    this.portNum = portNum;
  }

  public void start() throws IOException {
    DatagramSocket socket = new DatagramSocket(portNum);
    byte[] buffer = new byte[256];
    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

    while (true) {
      socket.receive(packet);
    }
  }

  public static void main(String[] args) throws IOException {
    Integer portNum = Integer.parseInt(args[0]);

    Server server = new Server(portNum);
    server.start();
  }
}
