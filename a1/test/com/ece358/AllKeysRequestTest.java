package com.ece358;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.*;

public class AllKeysRequestTest {
  @Test public void toBytesAndBackWorks() {
    AllKeysRequest request = new AllKeysRequest();
    byte[] bytes = request.toBytes();
    AllKeysRequest reformedRequest =
        (AllKeysRequest) RequestParser.fromString(new String(bytes, StandardCharsets.US_ASCII));
    assertTrue(reformedRequest != null);
  }
}
