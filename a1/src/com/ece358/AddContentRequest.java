package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-04.
 */
public class AddContentRequest extends Request {
  public final String address;
  public final Integer port;
  public final String content;

  public AddContentRequest(String address, Integer port, String content) {
    this.address = address;
    this.port = port;
    this.content = content;
  }

  @Override
  byte[] toBytes() {
    String request = String.format("%s:%s:%s", address, port, content);
    return new StringBuilder()
        .append("ADDCONTENT\n")
        .append("Content-Length: " + request.length() + "\n")
        .append("\n")
        .append(request)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}