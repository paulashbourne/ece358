package com.ece358;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RemovePeerTest {
  @Test public void canRemovePeers() throws Exception {
    // Remove the only peer
    PeerProcess peerProcess = new PeerProcess();
    Request request = new RemovePeerRequest("127.0.0.1", 8000);
    RemovePeerResponse response = (RemovePeerResponse) peerProcess.handleRequest(request);
    assertEquals(response.success, true);
  }
}
