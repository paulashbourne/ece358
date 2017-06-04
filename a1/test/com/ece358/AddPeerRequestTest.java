package com.ece358;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jeff on 2017-06-03.
 */
public class AddPeerRequestTest {
  @Test public void toBytesAndBack1() {
    String address = "localhost";
    Integer port = 8000;
    AddPeerRequest request = new AddPeerRequest(address, port);
    byte[] bytes = request.toBytes();

    AddPeerRequest reformedRequest =
        (AddPeerRequest) RequestParser.fromString(new String(bytes, StandardCharsets.US_ASCII));

    assertEquals(reformedRequest.address, address);
    assertEquals(reformedRequest.port, port);
  }

  @Test public void toBytesAndBack2() {
    String address = "123.456.789.000";
    Integer port = 12345;
    AddPeerRequest request = new AddPeerRequest(address, port);
    byte[] bytes = request.toBytes();

    AddPeerRequest reformedRequest =
        (AddPeerRequest) RequestParser.fromString(new String(bytes, StandardCharsets.US_ASCII));

    assertEquals(reformedRequest.address, address);
    assertEquals(reformedRequest.port, port);
  }
}