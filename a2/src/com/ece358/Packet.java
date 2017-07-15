package com.ece358;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * 0   --- 2 bytes ---   15                  31
 * -------------------------------------------
 * |       source        |   destination     |
 * -------------------------------------------
 * |               segment size              |
 * -------------------------------------------
 * |              sequence number            |
 * -------------------------------------------
 * |           acknowledgement number        |
 * -------------------------------------------
 * |S|A|F|     unused    |      checksum     |
 * -------------------------------------------
 * |                  payload                |
 * |                           ---------------
 * |                           |
 * -----------------------------
 *
 * MSB is used for multi-byte segments
 */
public class Packet {
  public final int sourcePort;
  public final int destinationPort;
  public final int segmentSize;
  public final int sequenceNumber;
  public final int ackNumber;
  public final boolean SYN;
  public final boolean ACK;
  public final boolean FIN;
  public final int checksum;
  public final byte[] payload;

  public Packet(byte[] packetData) {
    sourcePort = bytesToInt(packetData[0], packetData[1]);
    destinationPort = bytesToInt(packetData[2], packetData[3]);
    segmentSize = bytesToInt(packetData[4], packetData[5], packetData[6], packetData[7]);
    sequenceNumber = bytesToInt(packetData[8], packetData[9], packetData[10], packetData[11]);
    ackNumber = bytesToInt(packetData[12], packetData[13], packetData[14], packetData[15]);
    SYN = (packetData[16] & 0b10000000) > 0;
    ACK = (packetData[16] & 0b01000000) > 0;
    FIN = (packetData[16] & 0b00100000) > 0;
    checksum = bytesToInt(packetData[18], packetData[19]);
    if (packetData.length == 20) {
      payload = new byte[0];
    } else {
      payload = Arrays.copyOfRange(packetData, 20, packetData.length);
    }
  }

  public static Packet fromDatagram(DatagramPacket datagramPacket) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(datagramPacket.getData());
    byte[] data = new byte[datagramPacket.getLength()];
    byteBuffer.get(data, datagramPacket.getOffset(), datagramPacket.getLength());
    return new Packet(data);
  }

  private int bytesToInt(byte... data) {
    int result = 0;
    for (int i = 0; i < data.length; i++) {
      if (data[i] < 0) {
        result += (256 + data[i]) << ((data.length - i - 1) * 8);
      } else {
        result += data[i] << ((data.length - i - 1) * 8);
      }
    }
    return result;
  }

  private Packet(Builder builder) {
    this.sourcePort = builder.sourcePort;
    this.destinationPort = builder.destinationPort;
    this.segmentSize = builder.segmentSize;
    this.sequenceNumber = builder.sequenceNumber;
    this.ackNumber = builder.ackNumber;
    this.SYN = builder.SYN;
    this.ACK = builder.ACK;
    this.FIN = builder.FIN;
    this.payload = builder.payload;
    try {
      this.checksum = calculateChecksum(this.toBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private int calculateChecksum(byte[] bytes) {
    int checksum = 0;
    for (int i = 0; i < bytes.length; i += 2) {
      if (i == 18) {
        continue;
      }
      checksum += bytesToInt(bytes[i], bytes[i + 1]);
    }

    if (bytes.length % 2 == 1) {
      checksum += bytesToInt(bytes[bytes.length - 1], (byte) 0);
    }

    checksum = (checksum >> 16) + (checksum & 0xFFFF);

    return checksum ^ 0xFFFF;
  }

  public boolean hasErrors() {
    byte[] bytes;
    try {
      bytes = this.toBytes();
    } catch (IOException e) {
      return true;
    }

    int checksum = 0;
    for (int i = 0; i < bytes.length; i += 2) {
      checksum += bytesToInt(bytes[i], bytes[i + 1]);
    }

    if (bytes.length % 2 == 1) {
      checksum += bytesToInt(bytes[bytes.length - 1], (byte) 0);
    }

    checksum = (checksum >> 16) + (checksum & 0xFFFF);

    return (checksum ^ 0xFFFF) != 0;
  }

  byte[] toBytes() throws IOException {
    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

    byteStream.write(sourcePort >> 8);
    byteStream.write(sourcePort & 0xFF);

    byteStream.write(destinationPort >> 8);
    byteStream.write(destinationPort & 0xFF);

    byteStream.write(segmentSize >> 24);
    byteStream.write((segmentSize & 0xFF0000) >> 16);
    byteStream.write((segmentSize & 0xFF00) >> 8);
    byteStream.write(segmentSize & 0xFF);

    byteStream.write(sequenceNumber >> 24);
    byteStream.write((sequenceNumber & 0xFF0000) >> 16);
    byteStream.write((sequenceNumber & 0xFF00) >> 8);
    byteStream.write(sequenceNumber & 0xFF);

    byteStream.write(ackNumber >> 24);
    byteStream.write((ackNumber & 0xFF0000) >> 16);
    byteStream.write((ackNumber & 0xFF00) >> 8);
    byteStream.write(ackNumber & 0xFF);

    byte controlFlowByte = 0;
    if (SYN) {
      controlFlowByte += 0b10000000;
    }
    if (ACK) {
      controlFlowByte += 0b01000000;
    }
    if (FIN) {
      controlFlowByte += 0b00100000;
    }
    byteStream.write(controlFlowByte);

    byteStream.write(0x00);

    byteStream.write(checksum >> 8);
    byteStream.write(checksum & 0xFF);

    if (payload != null) {
      byteStream.write(payload);
    }

    return byteStream.toByteArray();
  }

  @Override
  public boolean equals(Object o) {
    if (o.getClass() != Packet.class) {
      return false;
    }
    Packet other = (Packet) o;

    return sourcePort == other.sourcePort
        && destinationPort == other.destinationPort
        && segmentSize == other.segmentSize
        && sequenceNumber == other.sequenceNumber
        && ackNumber == other.ackNumber
        && SYN == other.SYN
        && ACK == other.ACK
        && FIN == other.FIN
        && checksum == other.checksum
        && Arrays.equals(payload, other.payload);
  }

  public static class Builder {
    int sourcePort;
    int destinationPort;
    int segmentSize;
    int sequenceNumber;
    int ackNumber;
    boolean SYN;
    boolean ACK;
    boolean FIN;
    byte[] payload;

    public Builder sourcePort(int sourcePort) {
      this.sourcePort = sourcePort;
      return this;
    }

    public Builder destinationPort(int destinationPort) {
      this.destinationPort = destinationPort;
      return this;
    }

    public Builder segmentSize(int segmentSize) {
      this.segmentSize = segmentSize;
      return this;
    }

    public Builder sequenceNumber(int sequenceNumber) {
      this.sequenceNumber = sequenceNumber;
      return this;
    }

    public Builder ackNumber(int ackNumber) {
      this.ackNumber = ackNumber;
      return this;
    }

    public Builder SYN(boolean SYN) {
      this.SYN = SYN;
      return this;
    }

    public Builder ACK(boolean ACK) {
      this.ACK = ACK;
      return this;
    }

    public Builder FIN(boolean FIN) {
      this.FIN = FIN;
      return this;
    }

    public Builder payload(byte[] payload) {
      this.payload = payload;
      return this;
    }

    public Packet build() {
      return new Packet(this);
    }
  }
}
