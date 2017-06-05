package com.ece358;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

/**
 * Created by jeff on 2017-06-03.
 */
public class AddPeerResponse extends Response {
  public final boolean success;
  public Set<Peer> peers;

  public AddPeerResponse(boolean success, Set<Peer> peers) {
    this.success = success;
    this.peers = peers;
  }

  @Override public byte[] toBytes() {
    StringBuilder sb = new StringBuilder("ADDPEER");
    if (!success) {
      sb.append("\nFAILURE");
    } else {
      sb.append("\nSUCCESS");
    }

    StringBuilder peerStringBuilder = new StringBuilder();
    for (Peer peer : peers) {
      peerStringBuilder.append("\n");
      peerStringBuilder.append(String.format("%s:%s", peer.getAddress(), peer.getPort()));
    }
    String peerString = peerStringBuilder.toString();
    sb.append(String.format("\nContent-Length: %s\n", peerString.length()));

    return sb.append(peerString).toString().getBytes(StandardCharsets.US_ASCII);
  }
}
