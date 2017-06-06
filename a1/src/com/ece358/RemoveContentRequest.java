package com.ece358;

import java.nio.charset.StandardCharsets;

public class RemoveContentRequest extends Request {

  public Integer key;

  public RemoveContentRequest(Integer key) {
    this.key = key;
  }

  @Override
  byte[] toBytes() {
    String request = String.format("%s", key);
    return new StringBuilder()
        .append("REMOVECONTENT\n")
        .append("Content-Length: " + request.length() + "\n")
        .append("\n")
        .append(request)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
