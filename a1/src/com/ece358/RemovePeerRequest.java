package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by paul on 2017-06-04.
 */
public class RemovePeerRequest extends Request {

  public RemovePeerRequest() {
  }

  @Override byte[] toBytes() {
    return new StringBuilder()
        .append("REMOVEPEER\n")
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
