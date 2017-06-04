package com.ece358;

/**
 * Of the form:
 *
 * (SUCCESS|FAILURE)
 * Content-Length: nnn
 *
 * Content
 */
public abstract class Response extends Message {
  static Response fromString(String s) {
    return ResponseParser.fromString(s);
  }
}
