package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.scanner.Token;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;

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

  // TODO Exercise 5-6: Code generation
  // ===============================================

  // Error distance
  private final int MIN_ERROR_DISTANCE = 3;
  private int errorDistance = MIN_ERROR_DISTANCE;

  // Sets to handle certain first, follow, and recover sets
  private final EnumSet<Token.Kind> startOfStatement = EnumSet.of(ident, if_, while_, break_, return_, read, print, lbrace, semicolon);
  private final EnumSet<Token.Kind> startOfAssignop = EnumSet.of(assign, plusas, minusas, timesas, slashas, remas);
  private final EnumSet<Token.Kind> startOfFactor = EnumSet.of(ident, number, charConst, new_, lpar);

  private final EnumSet<Token.Kind> recoverDeclSet = EnumSet.of(final_, ident, class_, rbrace, eof);
  private final EnumSet<Token.Kind> recoverStatementSet = EnumSet.of(if_, while_, break_, return_, read, print, semicolon, eof);

  // ---------------------------------
  // One top-down parsing method per production

  private void program(){
    check(program);
    check(ident);
    Obj progObj = tab.insert(Obj.Kind.Prog, t.val, Tab.noType);
    tab.openScope();
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
    if (tab.curScope.nVars() > MAX_GLOBALS){
      error(TOO_MANY_GLOBALS);
    }
    check(lbrace);
    while (true){
      if (sym == ident || sym == void_) {
        methodDecl();
      } else if (sym == rbrace || sym == eof) {
        break;
      } else {
        recoverMethod();
      }
    }
    check(rbrace);

    progObj.locals = tab.curScope.locals();
    tab.closeScope();
  }

  private void constDecl(){
    check (final_);
    Struct type = type();
    check(ident);
    Obj constObj = tab.insert(Obj.Kind.Con, t.val, type);
    check(assign);
    if (sym == number){
      if (type.kind != Struct.Kind.Int){
        error(CONST_TYPE);
      }
      scan();
      constObj.val = t.numVal;
    } else if (sym == charConst){
      if (type.kind != Struct.Kind.Char){
        error(CONST_TYPE);
      }
      scan();
      constObj.val = t.numVal;
    } else {
      error(CONST_DECL);
    }
    check(semicolon);
  }

  private void varDecl(){
    Struct type = type();
    check(ident);
    tab.insert(Obj.Kind.Var, t.val, type);
    while (sym == comma){
      scan();
      check(ident);
      tab.insert(Obj.Kind.Var, t.val, type);
    }
    check(semicolon);
  }

  private void classDecl(){
    check(class_);
    check(ident);
    Obj c = tab.insert(Obj.Kind.Type, t.val, new Struct(Struct.Kind.Class));
    check(lbrace);
    tab.openScope();
    while (sym == ident){
      varDecl();
    }
    if (tab.curScope.nVars() > MAX_FIELDS){
      error(TOO_MANY_FIELDS);
    }
    c.type.fields = tab.curScope.locals();
    tab.closeScope();
    check(rbrace);
  }

  private Obj methodDecl(){
    Struct type = Tab.noType;
    if (sym == ident){
      type = type();
    } else if (sym == void_){
      scan();
    } else {
      error(INVALID_METH_DECL);
    }
    check(ident);
    Obj meth = tab.insert(Obj.Kind.Meth, t.val, type);
    meth.adr = code.pc;
    check(lpar);
    tab.openScope();
    if (sym == ident){
      formPars();
    }
    meth.nPars = tab.curScope.nVars();
    check(rpar);
    if (meth.name.equals("main")) {
      if (type != Tab.noType){
        error(MAIN_NOT_VOID);
      }
      if (meth.nPars != 0){
        error(MAIN_WITH_PARAMS);
      }
    }
    while (sym == ident){
      varDecl();
    }
    if (tab.curScope.nVars() > MAX_LOCALS){
      error(TOO_MANY_LOCALS);
    }
    block();

    meth.locals = tab.curScope.locals();
    tab.closeScope();

    return meth;
  }

  private void formPars(){
    Struct type = type();
    check(ident);
    tab.insert(Obj.Kind.Var, t.val, type);
    while (sym == comma){
      scan();
      type = type();
      check(ident);
      tab.insert(Obj.Kind.Var, t.val, type);
    }
  }

  private Struct type(){
    check(ident);
    Obj o = tab.find(t.val);
    if (o.kind != Obj.Kind.Type){
      error(NO_TYPE);
    }
    Struct type = o.type;
    if (sym == lbrack){
      scan();
      check(rbrack);
      type = new Struct(type);
    }
    return type;
  }

  private void block(){
    check(lbrace);

    while (true){
      if (startOfStatement.contains(sym)){
        statement();
      } else if (sym == rbrace || sym == eof){
        break;
      } else {
        recoverStatement();
      }
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
        } else {
          switch(sym){
            case lpar:
              actPars();
              break;
            case pplus:
              scan();
              break;
            case mminus:
              scan();
              break;
            default: error(DESIGN_FOLLOW);
          }
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

  // Error recovery methods
  private void recoverDeclaration(){
    error(INVALID_DECL);
    do {
      scan();
    } while (!recoverDeclSet.contains(sym));
    errorDistance = 0;
  }

  private void recoverMethod(){
    error(INVALID_METH_DECL);
    do {
      scan();
    } while (sym != ident && sym != void_ && sym != eof);
    errorDistance = 0;
  }

  private void recoverStatement(){
    error(INVALID_STAT);
    do {
      scan();
    } while (!recoverStatementSet.contains(sym));
    errorDistance = 0;
  }

  // ====================================
  // ====================================
}
