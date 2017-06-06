//package com.ece358;
//
//import java.nio.charset.StandardCharsets;
//import java.util.LinkedList;
//import java.util.List;
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class AllKeysResponseTest {
//  @Test public void responseWithNoKeys() {
//    boolean success = true;
//    List<Integer> keys = new LinkedList<>();
//    AllKeysResponse response = new AllKeysResponse(success, keys);
//    byte[] bytes = response.toBytes();
//
//    AllKeysResponse reformedResponse =
//        (AllKeysResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));
//    assertNotNull(reformedResponse);
//    assertEquals(reformedResponse.success, true);
//    assertEquals(reformedResponse.keys.size(), 0);
//  }
//
//  @Test public void responseWithThreeKeys() {
//    boolean success = true;
//    List<Integer> keys = new LinkedList<>();
//    keys.add(1);
//    keys.add(2);
//    keys.add(3);
//    AllKeysResponse response = new AllKeysResponse(success, keys);
//    byte[] bytes = response.toBytes();
//
//    AllKeysResponse reformedResponse =
//        (AllKeysResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));
//    assertNotNull(reformedResponse);
//    assertEquals(reformedResponse.success, true);
//    assertEquals(reformedResponse.keys.get(0), (Integer) 1);
//    assertEquals(reformedResponse.keys.get(1), (Integer) 2);
//    assertEquals(reformedResponse.keys.get(2), (Integer) 3);
//  }
//}
