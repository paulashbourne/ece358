package com.ece358;

import java.nio.charset.StandardCharsets;

public class LookupContentRequest extends Request {
  public String key;

  public LookupContentRequest(String key) {
    this.key = key;
  }

  @Override byte[] toBytes() {
    String content = key;
    return new StringBuilder()
        .append("LOOKUPCONTENT\n")
        .append("Content-Length: " + content.length() + "\n")
        .append("\n")
        .append(content)
        .toString().getBytes(StandardCharsets.US_ASCII);
  }
}
