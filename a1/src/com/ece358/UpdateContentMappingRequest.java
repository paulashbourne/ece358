package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-04.
 */
public class UpdateContentMappingRequest extends Request {
  String address;
  int port;
  int key;
  boolean add;

  public UpdateContentMappingRequest(String address, int port, int key, boolean add) {
    this.address = address;
    this.port = port;
    this.key = key;
    this.add = add;
  }

  @Override
  byte[] toBytes() {
    String request = String.format("%s:%s:%s", address, port, key);
    return new StringBuilder()
        .append("UPDATEMAPPING")
        .append((add ? "ADD\n" : "REMOVE\n"))
        .append("Content-Length: " + request.length() + "\n")
        .append("\n")
        .append(request)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
