package com.ece358;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class Utils {
  public static DatagramPacket packetToDatagramPacket(Packet packet, String destinationIpAddress,
      int destinationPort) throws IOException {
    int payloadSize = packet.payload != null ? packet.payload.length : 0;
    return new DatagramPacket(packet.toBytes(), payloadSize + 20,
        InetAddress.getByName(destinationIpAddress), destinationPort);
  }
}
