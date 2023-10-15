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
    nextCh(); // read 1st char into ch, increase col to 1
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

  /**
   * Mapping from keyword names to appropriate token codes.
   */
  private static final Map<String, Token.Kind> keywords;

  static {
    keywords = new HashMap<>();
    keywords.put("break", break_);
    keywords.put("class", class_);
    keywords.put("else", else_);
    keywords.put("final", final_);
    keywords.put("if", if_);
    keywords.put("new", new_);
    keywords.put("print", print);
    keywords.put("program", program);
    keywords.put("read", read);
    keywords.put("return", return_);
    keywords.put("void", void_);
    keywords.put("while", while_);
  }

  /**
   * Returns next token. To be used by parser.
   */
  public Token next() {
    while (Character.isWhitespace(ch)){
      nextCh();
    }
    Token t = new Token(none, line, col);
    switch (ch) {
      case 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> readName(t);
      case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> readNumber(t);
      case '\'' -> readCharCon(t);
      case '+' -> {
        nextCh();
        if (ch == '+') {
          t.kind = pplus;
          nextCh();
        } else if (ch == '=') {
          t.kind = plusas;
          nextCh();
        } else {
          t.kind = plus;
        }
      }
      case '-' -> {
        nextCh();
        if (ch == '-') {
          t.kind = mminus;
          nextCh();
        } else if (ch == '=') {
          t.kind = minusas;
          nextCh();
        } else {
          t.kind = minus;
        }
      }
      case '*' -> {
        nextCh();
        if (ch == '*') {
          t.kind = exp;
          nextCh();
        } else if (ch == '=') {
          t.kind = timesas;
          nextCh();
        } else {
          t.kind = times;
        }
      }
      case '/' -> {
        nextCh();
        if (ch == '*') {
          nextCh();
          skipComment(t);
          return next();
        } else if (ch == '=') {
          t.kind = slashas;
          nextCh();
        } else {
          t.kind = slash;
        }
      }
      case '%' -> {
        nextCh();
        if (ch == '=') {
          t.kind = remas;
          nextCh();
        } else {
          t.kind = rem;
        }
      }
      case '=' -> {
        nextCh();
        if (ch == '=') {
          t.kind = eql;
          nextCh();
        } else {
          t.kind = assign;
        }
      }
      case '!' -> {
        nextCh();
        if (ch == '=') {
          t.kind = neq;
          nextCh();
        } else {
          error(t, INVALID_CHAR, '!');
        }
      }
      case '>' -> {
        nextCh();
        if (ch == '=') {
          t.kind = geq;
          nextCh();
        } else {
          t.kind = gtr;
        }
      }
      case '<' -> {
        nextCh();
        if (ch == '=') {
          t.kind = leq;
          nextCh();
        } else {
          t.kind = lss;
        }
      }
      case '&' -> {
        nextCh();
        if (ch == '&') {
          t.kind = and;
          nextCh();
        } else {
          error(t, INVALID_CHAR, '&');
        }
      }
      case '|' -> {
        nextCh();
        if (ch == '|') {
          t.kind = or;
          nextCh();
        } else {
          error(t, INVALID_CHAR, '|');
        }
      }
      case '(' -> {
        t.kind = lpar;
        nextCh();
      }
      case ')' -> {
        t.kind = rpar;
        nextCh();
      }
      case '[' -> {
        t.kind = lbrack;
        nextCh();
      }
      case ']' -> {
        t.kind = rbrack;
        nextCh();
      }
      case '{' -> {
        t.kind = lbrace;
        nextCh();
      }
      case '}' -> {
        t.kind = rbrace;
        nextCh();
      }
      case ';' -> {
        t.kind = semicolon;
        nextCh();
      }
      case ',' -> {
        t.kind = comma;
        nextCh();
      }
      case '.' -> {
        t.kind = period;
        nextCh();
      }
      case EOF -> t.kind = eof;
      default -> {
        error(t, INVALID_CHAR, ch);
        nextCh();
      }
    }
    return t;
  }

  /**
   * Reads next character from input stream into ch. Keeps pos, line and col
   * in sync with reading position.
   */
  private void nextCh() {
    try {
      ch = (char) in.read();
      if (ch == LF){
        line++;
        col = 0;
      } else {
        col++;
      }
    } catch (IOException e) {
      ch = EOF;
    }
  }

  private void readName(Token t){
    StringBuilder sb = new StringBuilder();
    while (isLetter(ch) || isDigit(ch) || ch == '_'){
      sb.append(ch);
      nextCh();
    }
    t.val = sb.toString();
    t.kind = keywords.getOrDefault(t.val, ident);
  }

  private void readNumber(Token t){
    StringBuilder sb = new StringBuilder();
    while (isDigit(ch)){
      sb.append(ch);
      nextCh();
    }

    t.kind = number;
    t.val = sb.toString();
    try {
      t.numVal = Integer.parseInt(t.val);
    } catch(NumberFormatException e){
      error(t, BIG_NUM, t.val);
    }
  }

  private void readCharCon(Token t){
    nextCh();
    t.kind = charConst;

    if (ch == '\''){
      t.val = "";
      error(t, EMPTY_CHARCONST);
      nextCh();
    } else if (ch == LF || ch == '\r'){
      t.val = String.valueOf(ch);
      error(t, ILLEGAL_LINE_END);
      nextCh();
    } else if (ch == EOF){
      t.val = "EOF";
      error(t, EOF_IN_CHAR);
    } else if (ch == '\\') {
      nextCh();
      t.val = "\\" + ch;
      if (ch == 'r'){
        t.numVal = '\r';
      } else if (ch == 'n'){
        t.numVal = LF;
      } else if (ch == '\''){
        t.numVal = '\'';
      } else if (ch == '\\'){
        t.numVal = '\\';
      } else {
        error(t, UNDEFINED_ESCAPE, ch);
      }
      checkIfCharConstIsClosed(t);
    } else {
      t.val = String.valueOf(ch);
      t.numVal = ch;
      checkIfCharConstIsClosed(t);
    }
  }

  private void checkIfCharConstIsClosed(Token t){
    nextCh();
    if (ch == '\''){
      nextCh();
    } else {
      error(t, MISSING_QUOTE);
    }
  }

  private void skipComment(Token t){
    int depth = 1;
    while (depth > 0){
      if (ch == '/'){
        nextCh();
        if (ch == '*'){
          nextCh();
          depth++;
        }
      } else if (ch == '*'){
        nextCh();
        if (ch == '/'){
          nextCh();
          depth--;
        }
      } else if (ch == EOF){
        error(t, EOF_IN_COMMENT);
        break;
      } else {
        nextCh();
      }
    }
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
