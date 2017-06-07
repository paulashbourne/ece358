package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-04.
 */
public class UpdateContentMappingResponse extends Response {
  public final boolean success;

  public UpdateContentMappingResponse(boolean success) {
    this.success = success;
  }

  @Override public byte[] toBytes() {
    StringBuilder sb = new StringBuilder("UPDATEMAPPING");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    sb.append("Content-length: 0\n");

    return sb.toString().getBytes(StandardCharsets.US_ASCII);
  }
}
