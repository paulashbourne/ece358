package com.ece358;

import java.nio.ByteBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PacketTest {
  int sourcePort = 11_000;
  int destinationPort = 12_000;
  int segmentSize = 1024;
  int sequenceNumber = 3;
  int ackNumber = 5;
  boolean SYN = true;
  boolean ACK = false;
  boolean FIN = true;
  byte[] payload = new byte[] { 0x4, 0x1, 0x9, 0x78 };

  @Test public void canConvertWithEmptyPayload() throws Exception {
    Packet packet = new Packet.Builder()
        .sourcePort(sourcePort)
        .destinationPort(destinationPort)
        .segmentSize(segmentSize)
        .sequenceNumber(sequenceNumber)
        .ackNumber(ackNumber)
        .SYN(SYN)
        .ACK(ACK)
        .FIN(FIN)
        .payload(new byte[] {})
        .build();

    assertEquals(packet, new Packet(packet.toBytes()));
  }

  @Test public void canConvertWithNonEmptyPayload() throws Exception {
    Packet packet = new Packet.Builder()
        .sourcePort(sourcePort)
        .destinationPort(destinationPort)
        .segmentSize(segmentSize)
        .sequenceNumber(sequenceNumber)
        .ackNumber(ackNumber)
        .SYN(SYN)
        .ACK(ACK)
        .FIN(FIN)
        .payload(payload)
        .build();

    assertEquals(packet, new Packet(packet.toBytes()));
  }

  @Test public void unalteredPacketHasCorrectChecksum() throws Exception {
    Packet packet = new Packet.Builder()
        .sourcePort(sourcePort)
        .destinationPort(destinationPort)
        .segmentSize(segmentSize)
        .sequenceNumber(sequenceNumber)
        .ackNumber(ackNumber)
        .SYN(SYN)
        .ACK(ACK)
        .FIN(FIN)
        .payload(payload)
        .build();

    assertFalse(packet.hasErrors());
  }

  @Test public void alteredPacketHasBadChecksum() throws Exception {
    Packet packet = new Packet.Builder()
        .sourcePort(sourcePort)
        .destinationPort(destinationPort)
        .segmentSize(segmentSize)
        .sequenceNumber(sequenceNumber)
        .ackNumber(ackNumber)
        .SYN(SYN)
        .ACK(ACK)
        .FIN(FIN)
        .payload(payload)
        .build();

    byte[] alteredBytes = ByteBuffer.wrap(packet.toBytes()).put((byte) 0xAB).array();
    assertTrue(new Packet(alteredBytes).hasErrors());
  }
}
