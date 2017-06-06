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
    } else if (s.startsWith("ADDCONTENT")) {
      return addContentRequestFromString(s);
    } else if (s.startsWith("ALLKEYS")) {
      return new AllKeysRequest();
    } else if (s.startsWith("UPDATECOUNTER")) {
      return updateCounterRequestFromString(s);
    } else if (s.startsWith("UPDATEMAPPING")) {
      return updateMappingRequestFromString(s);
    } else if (s.startsWith("LOOKUPCONTENT")) {
      return lookupContentRequestFromString(s);
    }

    return null;
  }

  private static LookupContentRequest lookupContentRequestFromString(String s) {
    String[] splitContent = verifyRequest(s);
    if (splitContent == null) {
      return null;
    }

    return new LookupContentRequest(splitContent[0]);
  }

  public static String[] verifyRequest(String s) {
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
      return splitRequest[3].trim().split(":");
    } else {
      return null;
    }
  }

  public static AddPeerRequest addPeerRequestFromString(String s) {
    String[] splitContent = verifyRequest(s);
    if (splitContent == null || splitContent.length < 2) {
      return null;
    }

    return new AddPeerRequest(splitContent[0], Integer.valueOf(splitContent[1]));
  }

  public static AddContentRequest addContentRequestFromString(String s) {
    String[] splitContent = verifyRequest(s);
    if (splitContent == null || splitContent.length < 3) {
      return null;
    }

    return new AddContentRequest(splitContent[0], Integer.valueOf(splitContent[1]), splitContent[2]);
  }

  public static UpdateCounterRequest updateCounterRequestFromString(String s) {
    String[] splitContent = verifyRequest(s);
    if (splitContent == null || splitContent.length < 3) {
      return null;
    }

    return new UpdateCounterRequest(splitContent[0], Integer.valueOf(splitContent[1]), Integer.valueOf(splitContent[2]));
  }

  public static UpdateContentMappingRequest updateMappingRequestFromString(String s) {
    String[] splitContent = verifyRequest(s);
    if (splitContent == null || splitContent.length < 3) {
      return null;
    }

    boolean add = s.contains("ADD");

    return new UpdateContentMappingRequest(
        splitContent[0],
        Integer.valueOf(splitContent[1]),
        Integer.valueOf(splitContent[2]),
        add
    );
  }
}
