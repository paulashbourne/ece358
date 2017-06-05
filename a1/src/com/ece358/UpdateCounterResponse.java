package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-04.
 */
public class UpdateCounterResponse extends Response {
  public boolean success;

  public UpdateCounterResponse(boolean success) {
    this.success = success;
  }

  @Override
  byte[] toBytes() {
    StringBuilder sb = new StringBuilder("UPDATECOUNTER");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    sb.append(String.format("\nContent-Length: %s\n", 0));

    return sb.toString().getBytes(StandardCharsets.US_ASCII);
  }
}
