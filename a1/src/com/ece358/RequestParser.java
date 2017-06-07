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
    } else if (s.startsWith("REMOVECONTENT")) {
      return removeContentRequestFromString(s);
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
    if (splitRequest.length < 4) {
      return null;
    }
    Matcher matcher = contentLengthPattern.matcher(splitRequest[1]);
    if (!matcher.matches()) {
      return null;
    }
    Integer contentLength = Integer.valueOf(matcher.group(1));
    if (splitRequest.length == 4 && contentLength == splitRequest[3].length()) {
      return splitRequest[3].trim().split(":");
    } else if (splitRequest.length == 5 && contentLength == splitRequest[4].length()) {
      String[] split = splitRequest[4].trim().split(":");
      return new String[] {split[0], split[1], split[2], splitRequest[3]};
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
    if (splitContent == null || splitContent.length < 4) {
      return null;
    }

    boolean propagate = splitContent[3].equals("NOPROPAGATE");

    return new AddContentRequest(
        splitContent[0],
        Integer.valueOf(splitContent[1]),
        splitContent[2],
        propagate
    );
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

  public static RemoveContentRequest removeContentRequestFromString(String s) {
    String[] splitContent = verifyRequest(s);
    if (splitContent == null || splitContent.length < 1) {
      return null;
    }

    return new RemoveContentRequest(Integer.valueOf(splitContent[0]));
  }
}
