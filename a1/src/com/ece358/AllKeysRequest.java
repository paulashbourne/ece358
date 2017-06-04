package com.ece358;

import java.nio.charset.StandardCharsets;

public class AllKeysRequest extends Request {
  @Override byte[] toBytes() {
    return new StringBuilder()
        .append("ALLKEYS\n")
        .append("Content-Length: 0\n")
        .toString().getBytes(StandardCharsets.US_ASCII);  }
}
