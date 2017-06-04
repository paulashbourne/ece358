package com.ece358;
import java.io.IOException;

public class RemovePeer {
  public static void main(String[] args) {
    // write your code here
    Request request = new RemovePeerRequest();
    try {
      RemovePeerResponse response = (RemovePeerResponse)
        Utils.sendAndGetResponse(args[0], Integer.valueOf(args[1]), request);
    } catch (IOException e) {
      System.err.println("Error: no such peer");
      System.exit(1);
    }
  }
}
