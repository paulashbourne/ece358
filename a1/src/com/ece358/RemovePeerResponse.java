package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by paul on 2017-06-04.
 */
public class RemovePeerResponse extends Response {
  public final boolean success;

  public RemovePeerResponse(boolean success) {
    this.success = success;
  }

  @Override public byte[] toBytes() {
    StringBuilder sb = new StringBuilder("REMOVEPEER");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }
    sb.append("Content-length: 0\n");

    return sb.toString().getBytes(StandardCharsets.US_ASCII);
  }
}
