package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by paul on 2017-06-04.
 */
public class RemovePeerRequest extends Request {
  public String address;
  public Integer port;

  public RemovePeerRequest(String address, Integer port) {
    this.address = address;
    this.port = port;
  }

  @Override byte[] toBytes() {
    String content = String.format("%s:%s", address, port);
    return new StringBuilder()
        .append("REMOVEPEER\n")
        .append("Content-Length: " + content.length() + "\n")
        .append("\n")
        .append(content)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
