package com.ece358;

import java.util.*;
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
    } else if (s.startsWith("ADDCONTENT")) {
      return addContentResponseFromString(s);
    } else if (s.startsWith("ALLKEYS")) {
      return allKeysResponseFromString(s);
    } else if (s.startsWith("UPDATECOUNTER")) {
      return updateCounterResponseFromString(s);
    }

    return null;
  }

  static AddPeerResponse addPeerResponseFromString(String s) {
    if (s.startsWith("ADDPEER\nFAILURE")) {
      return new AddPeerResponse(false, null);
    } else if (s.equals("ADDPEER\nSUCCESS\nContent-Length: 0\n\n")) {
      return new AddPeerResponse(true, new HashSet<>());
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
      Set<Peer> peers = new HashSet<>();
      for (int i = 4; i < splitRequest.length; i++) {
        String[] splitContent = splitRequest[i].split(":");
        peers.add(new Peer(splitContent[0], Integer.valueOf(splitContent[1])));
      }
      return new AddPeerResponse(true, peers);
    } else {
      return null;
    }
  }

  static AllKeysResponse allKeysResponseFromString(String s) {
    if (s.startsWith("ALLKEYS\nFAILURE")) {
      return new AllKeysResponse(false, new ArrayList<>());
    } else if (s.equals("ALLKEYS\nSUCCESS\nContent-Length: 0\n")) {
      return new AllKeysResponse(true, new ArrayList<>());
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

    System.out.println(s);

    if (contentLength == requestLength) {
      List<Integer> keys = new LinkedList<>();
      for (int i = 4; i < splitRequest.length; i++) {
        keys.add(Integer.valueOf(splitRequest[i]));
      }
      return new AllKeysResponse(true, keys);
    } else {
      return null;
    }
  }

  static AddContentResponse addContentResponseFromString(String s) {
    if (s.startsWith("ADDCONTENT\nFAILURE")) {
      return new AddContentResponse(false, -1);
    }

    String[] splitRequest = s.split("\n");
    if (splitRequest.length < 4) {
      return null;
    }

    Matcher matcher = contentLengthPattern.matcher(splitRequest[2]);
    if (!matcher.matches()) {
      return null;
    }

    Integer contentLength = Integer.valueOf(matcher.group(1));
    int requestLength = splitRequest[3].length();

    System.out.println(s);

    if (contentLength == requestLength) {
      return new AddContentResponse(true, Integer.valueOf(splitRequest[3]));
    } else {
      return null;
    }
  }

  static UpdateCounterResponse updateCounterResponseFromString(String s) {
    if (s.startsWith("UPDATECOUNTER\nSUCCESS")) {
      return new UpdateCounterResponse(true);
    } else if (s.startsWith("UPDATECOUNTER\nFAILURE")) {
      return new UpdateCounterResponse(false);
    }

    return null;
  }
}
