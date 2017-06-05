package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-04.
 */
public class UpdateCounterRequest extends Request {
  String address;
  int port;
  int counter;

  public UpdateCounterRequest(String address, int port, int counter) {
    this.address = address;
    this.port = port;
    this.counter = counter;
  }

  @Override
  byte[] toBytes() {
    String request = String.format("%s:%s:%s", address, port, counter);
    return new StringBuilder()
        .append("UPDATECOUNTER\n")
        .append("Content-Length: " + request.length() + "\n")
        .append("\n")
        .append(request)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
