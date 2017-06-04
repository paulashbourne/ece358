package com.ece358;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AddPeerTest {
  @Test public void canAddPeers() throws Exception {
    AddPeer addPeerNode = new AddPeer();
    Request request = new AddPeerRequest("127.0.0.1", 8000);
    AddPeerResponse response = (AddPeerResponse) addPeerNode.handleRequest(request);
    assertEquals(response.success, true);
    assertEquals(response.peers.size(), 1);
    assertEquals(response.peers.get(0).getPort(), (Integer) 8000);
    assertEquals(response.peers.get(0).getAddress(), "127.0.0.1");

    request = new AddPeerRequest("123.456.789.000", 8080);
    response = (AddPeerResponse) addPeerNode.handleRequest(request);
    assertEquals(response.success, true);
    assertEquals(response.peers.size(), 2);
    assertEquals(response.peers.get(0).getPort(), (Integer) 8000);
    assertEquals(response.peers.get(0).getAddress(), "127.0.0.1");
    assertEquals(response.peers.get(1).getPort(), (Integer) 8080);
    assertEquals(response.peers.get(1).getAddress(), "123.456.789.000");
  }
}