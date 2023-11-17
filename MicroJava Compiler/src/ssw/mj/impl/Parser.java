package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.scanner.Token;

import java.util.EnumSet;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.scanner.Token.Kind.*;

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
    errorDistance++;
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
    if (errorDistance >= MIN_ERROR_DISTANCE){
      scanner.errors.error(la.line, la.col, msg, msgParams);
    }
    errorDistance = 0;
  }

  /**
   * Starts the analysis.
   */
  public void parse() {
    scan();
    program();
    check(eof);
  }

  // ===============================================


  // TODO Exercise 3: Error recovery methods
  private void recoverDeclaration(){
    error(INVALID_DECL);
    do {
      scan();
    } while (!recoverDeclSet.contains(sym));
    errorDistance = 0;
  }



  // TODO Exercise 4: Symbol table handling
  // TODO Exercise 5-6: Code generation
  // ===============================================

  // TODO Exercise 3: Error distance
  private final int MIN_ERROR_DISTANCE = 3;
  private int errorDistance = MIN_ERROR_DISTANCE;

  // TODO Exercise 3: Sets to handle certain first, follow, and recover sets
  private final EnumSet<Token.Kind> startOfStatement = EnumSet.of(ident, if_, while_, break_, return_, read, print, lbrace, semicolon);
  private final EnumSet<Token.Kind> startOfAssignop = EnumSet.of(assign, plusas, minusas, timesas, slashas, remas);
  private final EnumSet<Token.Kind> startOfFactor = EnumSet.of(ident, number, charConst, new_, lpar);

  private final EnumSet<Token.Kind> recoverDeclSet = EnumSet.of(final_, ident, class_, rbrace, eof);
  // ---------------------------------

  private void program(){
    check(program);
    check(ident);
    while (true){
      if (sym == final_){
        constDecl();
      } else if (sym == ident){
        varDecl();
      } else if (sym == class_){
        classDecl();
      } else if (sym == lbrace || sym == eof){
        break;
      } else {
        recoverDeclaration();
      }
    }
    check(lbrace);
    while (sym == ident || sym == void_){
      methodDecl();
    }
    check(rbrace);
  }

  private void constDecl(){
    check (final_);
    type();
    check(ident);
    check(assign);
    if (sym == number){
      scan();
    } else if (sym == charConst){
      scan();
    } else {
      error(CONST_DECL);
    }
    check(semicolon);
  }

  private void varDecl(){
    type();
    check(ident);
    while (sym == comma){
      scan();
      check(ident);
    }
    check(semicolon);
  }

  private void classDecl(){
    check(class_);
    check(ident);
    check(lbrace);
    while (sym == ident){
      varDecl();
    }
    check(rbrace);
  }

  private void methodDecl(){
    if (sym == ident){
      type();
    } else if (sym == void_){
      scan();
    } else {
      error(INVALID_METH_DECL);
    }
    check(ident);
    check(lpar);
    if (sym == ident){
      formPars();
    }
    check(rpar);
    while (sym == ident){
      varDecl();
    }
    block();
  }

  private void formPars(){
    type();
    check(ident);
    while (sym == comma){
      scan();
      type();
      check(ident);
    }
  }

  private void type(){
    check(ident);
    if (sym == lbrack){
      scan();
      check(rbrack);
    }
  }

  private void block(){
    check(lbrace);
    while (startOfStatement.contains(sym)){
      statement();
    }
    check(rbrace);
  }

  private void statement(){
    switch(sym){
      case ident:
        designator();
        if (startOfAssignop.contains(sym)){
          assignop();
          expr();
        } else if (sym == lpar){
          actPars();
        } else if (sym == pplus){
          scan();
        } else if (sym == mminus){
          scan();
        } else {
          error(DESIGN_FOLLOW);
        }
        check(semicolon);
        break;
      case if_:
        scan();
        check(lpar);
        condition();
        check(rpar);
        statement();
        if (sym == else_){
          scan();
          statement();
        }
        break;
      case while_:
        scan();
        check(lpar);
        condition();
        check(rpar);
        statement();
        break;
      case break_:
        scan();
        check(semicolon);
        break;
      case return_:
        scan();
        if (sym == minus || startOfFactor.contains(sym)){
          expr();
        }
        check(semicolon);
        break;
      case read:
        scan();
        check(lpar);
        designator();
        check(rpar);
        check(semicolon);
        break;
      case print:
        scan();
        check(lpar);
        expr();
        if (sym == comma){
          scan();
          check(number);
        }
        check(rpar);
        check(semicolon);
        break;
      case lbrace:
        block();
        break;
      case semicolon:
        scan();
        break;
      default:
        error(INVALID_STAT);
    }
  }

  private void assignop(){
    switch(sym){
      case assign:
        scan();
        break;
      case plusas:
        scan();
        break;
      case minusas:
        scan();
        break;
      case timesas:
        scan();
        break;
      case slashas:
        scan();
        break;
      case remas:
        scan();
        break;
      default:
        error(ASSIGN_OP);
    }
  }

  private void actPars(){
    check(lpar);
    if (sym == minus || startOfFactor.contains(sym)){
      expr();
      while (sym == comma){
        scan();
        expr();
      }
    }
    check(rpar);
  }

  private void condition(){
    condTerm();
    while (sym == or){
      scan();
      condTerm();
    }
  }

  private void condTerm(){
    condFact();
    while (sym == and){
      scan();
      condFact();
    }
  }

  private void condFact(){
    expr();
    relop();
    expr();
  }

  private void relop(){
    switch (sym){
      case eql:
        scan();
        break;
      case neq:
        scan();
        break;
      case gtr:
        scan();
        break;
      case geq:
        scan();
        break;
      case lss:
        scan();
        break;
      case leq:
        scan();
        break;
      default:
        error(REL_OP);
    }
  }

  private void expr(){
    if (sym == minus){
      scan();
    }
    term();
    while (sym == plus || sym == minus) {
      addop();
      term();
    }
  }

  private void term(){
    factor();
    while (true){
      if (sym == times || sym == slash || sym == rem){
        mulop();
        factor();
      } else if (sym == exp){
        scan();
        check(number);
      } else {
        break;
      }
    }
  }

  private void factor(){
    switch (sym){
      case ident:
        designator();
        if (sym == lpar){
          actPars();
        }
        break;
      case number:
        scan();
        break;
      case charConst:
        scan();
        break;
      case new_:
        scan();
        check(ident);
        if (sym == lbrack){
          scan();
          expr();
          check(rbrack);
        }
        break;
      case lpar:
        scan();
        expr();
        check(rpar);
        break;
      default:
        error(INVALID_FACT);
    }
  }

  private void designator(){
    check(ident);
    while (true){
      if (sym == period){
        scan();
        check(ident);
      } else if (sym == lbrack) {
        scan();
        expr();
        check(rbrack);
      } else {
        break;
      }
    }
  }

  private void addop(){
    if (sym == plus){
      scan();
    } else if (sym == minus){
      scan();
    } else {
      error(ADD_OP);
    }
  }

  private void mulop(){
    if (sym == times){
      scan();
    } else if (sym == slash){
      scan();
    } else if (sym == rem){
      scan();
    } else {
      error(MUL_OP);
    }
  }
  // ------------------------------------

  // TODO Exercise 3: Error recovery methods: recoverDecl, recoverMethodDecl and recoverStat

  // ====================================
  // ====================================
}
