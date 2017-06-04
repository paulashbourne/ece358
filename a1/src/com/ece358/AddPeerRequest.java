package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by jeff on 2017-06-03.
 */
public class AddPeerRequest extends Request {
  public final String address;
  public final Integer port;

  public AddPeerRequest(String address, Integer port) {
    this.address = address;
    this.port = port;
  }

  @Override byte[] toBytes() {
    String content = String.format("%s:%s", address, port);
    return new StringBuilder()
        .append("ADDPEER\n")
        .append("Content-Length: " + content.length() + "\n")
        .append("\n")
        .append(content)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}