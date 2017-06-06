package com.ece358;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.*;

public class LookupContentResponseTest {
  @Test public void basicContent() throws Exception {
    boolean success = true;
    String content = "Hello world";
    LookupContentResponse response = new LookupContentResponse(success, content);
    byte[] bytes = response.toBytes();

    LookupContentResponse reformedResponse =
        (LookupContentResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));
    assertNotNull(reformedResponse);
    assertEquals(reformedResponse.success, true);
    assertEquals(reformedResponse.content, content);
  }

  @Test public void contentWithANewline() throws Exception {
    boolean success = true;
    String content = "Hello,\nworld";
    LookupContentResponse response = new LookupContentResponse(success, content);
    byte[] bytes = response.toBytes();

    LookupContentResponse reformedResponse =
        (LookupContentResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));
    assertNotNull(reformedResponse);
    assertEquals(reformedResponse.success, true);
    assertEquals(reformedResponse.content, content);
  }
}
