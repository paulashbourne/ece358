package com.ece358;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by jeff on 2017-06-03.
 */
public class AddPeerResponseTest {
  @Test public void successWithNoPeers() {
    boolean success = true;
    List<Peer> peers = new LinkedList<>();
    AddPeerResponse response = new AddPeerResponse(success, peers);
    byte[] bytes = response.toBytes();

    AddPeerResponse reformedResponse =
        (AddPeerResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));

    assertEquals(reformedResponse.success, true);
    assertEquals(reformedResponse.peers.size(), 0);
  }

  @Test public void successWithOnePeer() {
    boolean success = true;
    List<Peer> peers = new LinkedList<>();
    peers.add(new Peer("localhost", 8000));
    AddPeerResponse response = new AddPeerResponse(success, peers);
    byte[] bytes = response.toBytes();

    AddPeerResponse reformedResponse =
        (AddPeerResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));

    assertEquals(reformedResponse.success, true);
    assertEquals(reformedResponse.peers.size(), 1);
    assertEquals(reformedResponse.peers.get(0).getAddress(), "localhost");
    assertEquals(reformedResponse.peers.get(0).getPort(), (Integer) 8000);
  }

  @Test public void successWith10Peers() {
    boolean success = true;
    int numPeers = 10;
    List<Peer> peers = new LinkedList<>();
    for (int i = 0; i < numPeers; i++) {
      peers.add(new Peer("localhost" + i, i * 1000));
    }
    AddPeerResponse response = new AddPeerResponse(success, peers);
    byte[] bytes = response.toBytes();

    AddPeerResponse reformedResponse =
        (AddPeerResponse) ResponseParser.fromString(new String(bytes, StandardCharsets.US_ASCII));

    assertEquals(reformedResponse.success, true);
    assertEquals(reformedResponse.peers.size(), numPeers);
    for (int i = 0; i < numPeers; i++) {
      assertEquals(reformedResponse.peers.get(i).getAddress(), "localhost" + i);
      assertEquals(reformedResponse.peers.get(i).getPort(), (Integer) (i * 1000));
    }
  }
}