package com.ece358;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jeff on 2017-06-03.
 */
public class RequestParser {
  static Pattern contentLengthPattern = Pattern.compile("Content-Length: (\\d+)");

  public static Request fromString(String s) {
    if (s.startsWith("ADDPEER")) {
      return addPeerRequestFromString(s);
    } else if (s.startsWith("ALLKEYS")) {
      return new AllKeysRequest();
    }

    return null;
  }

  private static AddPeerRequest addPeerRequestFromString(String s) {
    String[] splitRequest = s.split("\n");
    if (splitRequest.length != 4) {
      return null;
    }
    Matcher matcher = contentLengthPattern.matcher(splitRequest[1]);
    if (!matcher.matches()) {
      return null;
    }
    Integer contentLength = Integer.valueOf(matcher.group(1));
    if (contentLength == splitRequest[3].length()) {
      String[] splitContent = splitRequest[3].trim().split(":");
      return new AddPeerRequest(splitContent[0], Integer.valueOf(splitContent[1]));
    } else {
      return null;
    }
  }
}
