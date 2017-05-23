package com.ece358;

public class PeerInfo {
  private String address;
  private Integer port;

  public PeerInfo(String address, Integer port) {
    this.address = address;
    this.port = port;
  }

  public String getAddress() {
    return address;
  }

  public Integer getPort() {
    return port;
  }
}
