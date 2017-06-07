package com.ece358;

public class Peer {
  private String address;
  private Integer port;

  public Peer(String address, Integer port) {
    this.address = address;
    this.port = port;
  }

  public String getAddress() {
    return address;
  }

  public Integer getPort() {
    return port;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Peer peer = (Peer) o;

    if (address != null ? !address.equals(peer.address) : peer.address != null) return false;
    return port != null ? port.equals(peer.port) : peer.port == null;

  }

  @Override
  public int hashCode() {
    int result = address != null ? address.hashCode() : 0;
    result = 31 * result + (port != null ? port.hashCode() : 0);
    return result;
  }
}
