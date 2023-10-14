package ssw.mj.impl;

import ssw.mj.Errors;
import ssw.mj.scanner.Token;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.scanner.Token.Kind.*;

public class Scanner {

  // Scanner Skeleton - do not rename fields / methods !
  private static final char EOF = (char) -1;
  private static final char LF = '\n';

  /**
   * Input data to read from.
   */
  private final Reader in;

  /**
   * Lookahead character. (= next (unhandled) character in the input stream)
   */
  private char ch;

  /**
   * Current line in input stream.
   */
  private int line;

  /**
   * Current column in input stream.
   */
  private int col;

  /**
   * According errors object.
   */
  public final Errors errors;

  public Scanner(Reader r) {
    // store reader
    in = r;

    // initialize error handling support
    errors = new Errors();

    line = 1;
    col = 0;
    nextCh(); // read 1st char into ch, incr col to 1
  }

  /**
   * Adds error message to the list of errors.
   */
  public final void error(Token t, Errors.Message msg, Object... msgParams) {
    errors.error(t.line, t.col, msg, msgParams);

    // reset token content (consistent JUnit tests)
    t.numVal = 0;
    t.val = null;
  }


  // ================================================
  // TODO Exercise 1: Implement Scanner (next() + private helper methods)
  // ================================================

  // TODO Exercise 1: Keywords
  /**
   * Mapping from keyword names to appropriate token codes.
   */
  private static final Map<String, Token.Kind> keywords;

  static {
    keywords = new HashMap<>();
  }

  /**
   * Returns next token. To be used by parser.
   */
  public Token next() {
    // TODO Exercise 1: implementation of next method
    return null;
  }

  // TODO Exercise 1: private helper methods used by next(), as discussed in the exercise

  /**
   * Reads next character from input stream into ch. Keeps pos, line and col
   * in sync with reading position.
   */
  private void nextCh() {
    // TODO Exercise 1
  }

  // ...

  private boolean isLetter(char c) {
    return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
  }

  private boolean isDigit(char c) {
    return '0' <= c && c <= '9';
  }

  // ================================================
  // ================================================
}
