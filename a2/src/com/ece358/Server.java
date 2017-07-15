package com.ece358;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Server {
  private Integer portNum;
  private String filePath;
  private Map<InetSocketAddress, SingleClientServer> addressToClientMapping;
  private List<Thread> runningThreads;

  public Server(Integer portNum, String filePath) {
    this.portNum = portNum;
    this.filePath = filePath;
    this.addressToClientMapping = new LinkedHashMap<>();
    this.runningThreads = new LinkedList<>();

    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      for (SingleClientServer server : addressToClientMapping.values()) {
        server.shutdown();
      }
    }));
  }

  public void start() throws IOException {
    DatagramSocket socket = new DatagramSocket(portNum);
    // TODO(jgulbronson) - figure out max packet size
    byte[] buffer = new byte[256];
    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
    while (true) {
      socket.receive(datagramPacket);
      byte[] packetData = datagramPacket.getData();
      Packet packet = new Packet(packetData);
      if (packet.hasErrors()) {
        continue;
      }

      String clientHostAddress = datagramPacket.getAddress().getCanonicalHostName();
      InetSocketAddress senderAddress = new InetSocketAddress(clientHostAddress, packet.sourcePort);
      if (!addressToClientMapping.containsKey(senderAddress)) {
        SingleClientServer singleClientServer =
            new SingleClientServer(clientHostAddress, packet.sourcePort, filePath);
        Thread thread = new Thread(singleClientServer);
        runningThreads.add(thread);
        thread.start();
        addressToClientMapping.put(senderAddress, singleClientServer);
        singleClientServer.addPacket(packet);
      } else {
        SingleClientServer singleClientServer = addressToClientMapping.get(senderAddress);
        if (singleClientServer.getState() == ServerState.ESTAB && packet.FIN) {
          singleClientServer.shutdown();
          packet = new Packet.Builder()
              .ACK(true)
              .destinationPort(datagramPacket.getPort())
              .sourcePort(portNum)
              .build();
          socket.send(
              Utils.packetToDatagramPacket(packet, datagramPacket.getAddress().getHostAddress(),
                  datagramPacket.getPort()));
          addressToClientMapping.remove(senderAddress);
        } else {
          singleClientServer.addPacket(packet);
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    Integer portNum = Integer.parseInt(args[0]);
    String filePath = args[1];

    Server server = new Server(portNum, filePath);
    server.start();
  }
}
