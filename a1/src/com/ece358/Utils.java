package com.ece358;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by jeff on 2017-06-03.
 */
public class Utils {
  public static Request readRequestFromInputStream(InputStream inputStream) throws IOException {
    byte readData[] = new byte[100];
    int bytesRead;
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    while (true) {
      if ((bytesRead = inputStream.read(readData)) > 0) {
        data.write(Arrays.copyOfRange(readData, 0, bytesRead));
        String message = data.toString(StandardCharsets.US_ASCII.name());
        Request request = RequestParser.fromString(message);
        if (request != null) {
          return request;
        }
      }
    }
  }

  public static Response readResponseFromInputStream(InputStream inputStream) throws IOException {
    byte readData[] = new byte[100];
    int bytesRead;
    ByteArrayOutputStream data = new ByteArrayOutputStream();
    while (true) {
      if ((bytesRead = inputStream.read(readData)) > 0) {
        data.write(Arrays.copyOfRange(readData, 0, bytesRead));
        String message = data.toString(StandardCharsets.US_ASCII.name());
        Response response = ResponseParser.fromString(message);
        if (response != null) {
          return response;
        }
      }
    }
  }
}