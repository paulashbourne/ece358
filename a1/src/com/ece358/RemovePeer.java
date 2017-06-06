package com.ece358;
import java.io.IOException;

public class RemovePeer {
  public static void main(String[] args) {
    // write your code here
    String address = args[0];
    Integer port = Integer.valueOf(args[1]);
    Request request = new RemovePeerRequest(address, port);
    try {
      RemovePeerResponse response = (RemovePeerResponse)
        Utils.sendAndGetResponse(address, port, request);
    } catch (IOException e) {
      System.err.println("Error: no such peer");
      System.exit(1);
    }
  }
}
