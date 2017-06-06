package com.ece358;

import java.nio.charset.StandardCharsets;

public class LookupContentResponse extends Response {
  public boolean success;
  public String content;

  public LookupContentResponse(boolean success, String content) {
    this.success = success;
    this.content = content;
  }

  @Override byte[] toBytes() {
    StringBuilder sb = new StringBuilder("LOOKUPCONTENT");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    sb.append(String.format("\nContent-Length: %s\n\n%s", content.length(), content));

    return sb.toString().getBytes(StandardCharsets.US_ASCII);  }
}
