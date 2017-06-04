package com.ece358;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AllKeys {
  public static void main(String[] args) {
    AllKeysRequest request = new AllKeysRequest();
    try {
      AllKeysResponse response =
          (AllKeysResponse) Utils.sendAndGetResponse(args[0], Integer.valueOf(args[1]), request);
      List<Integer> keys = new LinkedList<>();
      response.keys.forEach(keys::add);
      String joinedKeys = keys.stream()
          .map(n -> String.valueOf(n))
          .collect(Collectors.joining(" "));
      System.out.println(joinedKeys);
    } catch (IOException e) {
      System.err.println("Error: no such peer");
    }
  }
}
