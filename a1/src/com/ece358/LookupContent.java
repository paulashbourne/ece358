package com.ece358;

import java.io.IOException;

public class LookupContent {
  public static void main(String[] args) {
    if (args.length != 3) {
      System.err.println("You must pass 3 arguments");
      System.exit(-1);
    }

    String ipAddress = args[0];
    Integer port = Integer.valueOf(args[1]);
    String key = args[2];

    try {
      LookupContentResponse response =
          (LookupContentResponse) Utils.sendAndGetResponse(ipAddress, port, new LookupContentRequest(key));
      if (response.success) {
        System.out.println(response.content);
      } else {
        System.err.println("Error: no such content");
      }
    } catch (IOException e) {
      System.err.println("Error: no such peer");
    }
  }
}
