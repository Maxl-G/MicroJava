package ssw.mj.impl;

import ssw.mj.Errors;
import ssw.mj.Errors.Message;
import ssw.mj.scanner.Token;

import static ssw.mj.Errors.Message.TOKEN_EXPECTED;
import static ssw.mj.scanner.Token.Kind.none;

public final class Parser {

  /**
   * Maximum number of global variables per program
   */
  private static final int MAX_GLOBALS = 32767;

  /**
   * Maximum number of fields per class
   */
  private static final int MAX_FIELDS = 32767;

  /**
   * Maximum number of local variables per method
   */
  private static final int MAX_LOCALS = 127;

  /**
   * Last recognized token;
   */
  private Token t;

  /**
   * Lookahead token (not recognized).)
   */
  private Token la;

  /**
   * Shortcut to kind attribute of lookahead token (la).
   */
  private Token.Kind sym;

  /**
   * According scanner
   */
  public final Scanner scanner;

  /**
   * According code buffer
   */
  public final Code code;

  /**
   * According symbol table
   */
  public final Tab tab;

  public Parser(Scanner scanner) {
    this.scanner = scanner;
    tab = new Tab(this);
    code = new Code(this);
    // Pseudo token to avoid crash when 1st symbol has scanner error.
    la = new Token(none, 1, 1);
  }


  /**
   * Reads ahead one symbol.
   */
  private void scan() {
    t = la;
    la = scanner.next();
    sym = la.kind;
  }

  /**
   * Verifies symbol and reads ahead.
   */
  private void check(Token.Kind expected) {
    if (sym == expected) {
      scan();
    } else {
      error(TOKEN_EXPECTED, expected);
    }
  }

  /**
   * Adds error message to the list of errors.
   */
  public void error(Message msg, Object... msgParams) {
    // TODO Exercise 3: Replace panic mode with error recovery (i.e., keep track of error distance)
    // TODO Exercise 3: Hint: Replacing panic mode also affects scan() method
    scanner.errors.error(la.line, la.col, msg, msgParams);
    throw new Errors.PanicMode();
  }

  /**
   * Starts the analysis.
   */
  public void parse() {
    // TODO Exercise 2: Implementation of parser
  }

  // ===============================================
  // TODO Exercise 2: Implementation of parser
  // TODO Exercise 3: Error recovery methods
  // TODO Exercise 4: Symbol table handling
  // TODO Exercise 5-6: Code generation
  // ===============================================

  // TODO Exercise 3: Error distance

  // TODO Exercise 2 + Exercise 3: Sets to handle certain first, follow, and recover sets

  // ---------------------------------

  // TODO Exercise 2: One top-down parsing method per production

  // ------------------------------------

  // TODO Exercise 3: Error recovery methods: recoverDecl, recoverMethodDecl and recoverStat

  // ====================================
  // ====================================
}
