package com.ece358;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeff on 2017-06-03.
 */
public class ResponseParser {
  static Pattern contentLengthPattern = Pattern.compile("Content-Length: (\\d+)");

  static Response fromString(String s) {
    if (s.startsWith("ADDPEER")) {
      return addPeerResponseFromString(s);
    }

    return null;
  }

  static AddPeerResponse addPeerResponseFromString(String s) {
    if (s.startsWith("ADDPEER\nFAILURE")) {
      return new AddPeerResponse(false, null);
    } else if (s.equals("ADDPEER\nSUCCESS\nContent-Length: 0\n\n")) {
      return new AddPeerResponse(true, new ArrayList<>());
    }

    String[] splitRequest = s.split("\n");
    if (splitRequest.length < 5) {
      return null;
    }

    Matcher matcher = contentLengthPattern.matcher(splitRequest[2]);
    if (!matcher.matches()) {
      return null;
    }
    Integer contentLength = Integer.valueOf(matcher.group(1));

    int requestLength = 0;
    for (int i = 4; i < splitRequest.length; i++) {
      requestLength += splitRequest[i].length();
    }
    contentLength -= splitRequest.length - 4; // To account for newlines

    if (contentLength == requestLength) {
      List<Peer> peers = new LinkedList<>();
      for (int i = 4; i < splitRequest.length; i++) {
        String[] splitContent = splitRequest[i].split(":");
        peers.add(new Peer(splitContent[0], Integer.valueOf(splitContent[1])));
      }
      return new AddPeerResponse(true, peers);
    } else {
      return null;
    }
  }
}
