package com.ece358;

import java.io.IOException;

public class AddContent {
  public static void main(String[] args) {
    String ipAddress = args[0];
    int port = Integer.valueOf(args[1]);
    String content = args[2];

    AddContentRequest request = new AddContentRequest(ipAddress, port, content);
    try {
      AddContentResponse response = (AddContentResponse) Utils.sendAndGetResponse(ipAddress, port, request);
      if (!response.success) {
        System.err.println("Error: no such peer");
      }
    } catch (IOException e) {
      System.err.println("Error: no such peer");
    }
  }
}
