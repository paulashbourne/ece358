package com.ece358;

import java.nio.charset.StandardCharsets;

public class RemoveContentRequest extends Request {
  public final Integer key;
  public final boolean propagate;

  public RemoveContentRequest(Integer key, boolean propagate) {
    this.key = key;
    this.propagate = propagate;
  }

  @Override
  byte[] toBytes() {
    String request = String.format("%s", key);
    StringBuilder requestBuilder = new StringBuilder()
        .append((propagate ? "PROPAGATE\n" : "NOPROPAGATE\n"))
        .append(request);
    return new StringBuilder()
        .append("REMOVECONTENT\n")
        .append("Content-Length: " + request.length() + "\n")
        .append("\n")
        .append(requestBuilder.toString())
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
