package com.ece358;

/**
 * Of the form:
 *
 * (ADDCONTENT|ADDPEER|...)
 * Content-Length: nnn
 *
 * Content
 */
public abstract class Request extends Message {
  static Request fromString(String s) {
    return RequestParser.fromString(s);
  }
}
