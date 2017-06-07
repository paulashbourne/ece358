package com.ece358;

import java.nio.charset.StandardCharsets;

/**
 * Created by marcussjolin on 2017-06-05.
 */
public class RemoveContentResponse extends Response {
  public final boolean success;
  public final String content;

  public RemoveContentResponse(boolean success, String content) {
    this.success = success;
    this.content = content;
  }

  @Override public byte[] toBytes() {
    StringBuilder sb = new StringBuilder("REMOVECONTENT");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    sb.append(String.format("\nContent-Length: %s\n\n%s", content.length(), content));

    return sb.toString().getBytes(StandardCharsets.US_ASCII);
  }
}
