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
  byte[] payload = new byte[] {0x4, 0x1, 0x9, 0x78};

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

    assertEquals(packet, new Packet(packet.toBytes(), false));
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

    assertEquals(packet, new Packet(packet.toBytes(), false));
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
    assertTrue(new Packet(alteredBytes, false).hasErrors());
  }

  @Test public void worksWithTheChecksumExample() throws Exception {
    byte[] data = new byte[] {
        0b00001100, 0b00001000, 0b00010000, 0b00001000,
        0b00000000, 0b00000000, 0b00000000, 0b00010111,
        0b00000000, 0b00000000, 0b00000000, 0b00000011,
        0b00000000, 0b00000000, 0b00000000, 0b00000011,
        0b01000000, 0b00000000, 0b00000000, 0b00000000,
        0b01010101, 0b01010101, (byte) 0b11111111
    };

    Packet packet = new Packet(data, true);
    assertEquals(packet.checksum, 0b0100111101111100);

    assertFalse(packet.hasErrors());
  }
}
