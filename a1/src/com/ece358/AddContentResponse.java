package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-04.
 */
public class AddContentResponse extends Response {
  boolean success;
  int key;

  public AddContentResponse(boolean success, int key) {
    this.success = success;
    this.key = key;
  }

  @Override
  byte[] toBytes() {
    StringBuilder sb = new StringBuilder("ADDCONTENT");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    String keyStr = String.valueOf(key);

    sb.append(String.format("\nContent-Length: %s\n", keyStr.length()));

    return sb.append(keyStr).toString().getBytes(StandardCharsets.US_ASCII);
  }
}
