package com.ece358;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by jeff on 2017-06-03.
 */
public class AddPeerResponse extends Response {
  public final boolean success;
  public Set<Peer> peers;
  public int counter;
  public Map<Integer, Peer> peerContentMapping;

  public AddPeerResponse(boolean success, Set<Peer> peers, int counter, Map<Integer, Peer> peerContentMapping) {
    this.success = success;
    this.peers = peers;
    this.counter = counter;
    this.peerContentMapping = peerContentMapping;
  }

  @Override public byte[] toBytes() {
    StringBuilder sb = new StringBuilder("ADDPEER");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    StringBuilder peerStringBuilder = new StringBuilder();
    peerStringBuilder.append("\n").append(counter);

    for (Peer peer : peers) {
      peerStringBuilder.append("\n");
      peerStringBuilder.append(String.format("%s:%s", peer.getAddress(), peer.getPort()));
    }

    for (Integer key : peerContentMapping.keySet()) {
      Peer value = peerContentMapping.get(key);
      peerStringBuilder.append("\n");
      peerStringBuilder.append(String.format("%s:%s:%s", key, value.getAddress(), value.getPort()));
    }

    String peerString = peerStringBuilder.toString();
    sb.append(String.format("\nContent-Length: %s\n", peerString.length()));

    return sb.append(peerString).toString().getBytes(StandardCharsets.US_ASCII);
  }
}
