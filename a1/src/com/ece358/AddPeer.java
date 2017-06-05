package com.ece358;

import java.io.IOException;

public class AddPeer {
  public static void main(String[] args) throws IOException {
    PeerProcess peer = new PeerProcess();
    peer.start(args);
  }
}
