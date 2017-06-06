package com.ece358;

import java.io.IOException;

public class RemoveContent {
  public static void main(String[] args) {
    String address = args[0];
    int port = Integer.valueOf(args[1]);
    String key = args[2];

    RemoveContentRequest request = new RemoveContentRequest(Integer.valueOf(key));
    try {
      RemoveContentResponse response =
          (RemoveContentResponse) Utils.sendAndGetResponse(address, port, request);
      if (!response.success) {
        System.err.println("Error: no such content");
      }
    } catch (IOException e) {
      System.err.println("Error: no such peer");
    }
  }
}
