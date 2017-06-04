package com.ece358;

import java.nio.charset.StandardCharsets;

public class AllKeysResponse extends Response {
  public final boolean success;
  public Iterable<Integer> keys;

  public AllKeysResponse(boolean success, Iterable<Integer> keys) {
    this.success = success;
    this.keys = keys;
  }

  @Override byte[] toBytes() {
    StringBuilder sb = new StringBuilder("ALLKEYS");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    StringBuilder keyStringBuilder = new StringBuilder();
    for (Integer key: keys) {
      keyStringBuilder.append("\n");
      keyStringBuilder.append(key);
    }
    String keyString = keyStringBuilder.toString();
    sb.append(String.format("\nContent-Length: %s\n", keyString.length()));

    return sb.append(keyString).toString().getBytes(StandardCharsets.US_ASCII);
  }
}
