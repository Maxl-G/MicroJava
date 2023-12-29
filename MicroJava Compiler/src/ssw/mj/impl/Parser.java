package ssw.mj.impl;

import ssw.mj.Errors.Message;
import ssw.mj.codegen.Operand;
import ssw.mj.scanner.Token;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;

import java.util.EnumSet;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.scanner.Token.Kind.*;
import static ssw.mj.impl.Code.OpCode;

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

  // Sets for code generation
  private final EnumSet<Operand.Kind> assignableOperandKinds = EnumSet.of(Operand.Kind.Local, Operand.Kind.Static, Operand.Kind.Fld, Operand.Kind.Elem);


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

    if (code.mainpc == -1){
      error(METH_NOT_FOUND, "main");
    }
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
    Obj o = tab.insert(Obj.Kind.Var, t.val, type);
    if (o.level == 0){
      code.dataSize++;
    }
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
      code.mainpc = code.pc;
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
    code.put(OpCode.enter);
    code.put(meth.nPars);
    code.put(tab.curScope.nVars());
    block();

    meth.locals = tab.curScope.locals();
    tab.closeScope();
    if (meth.type == Tab.noType){
      code.put(OpCode.exit);
      code.put(OpCode.return_);
    } else {
      code.put(OpCode.trap);
      code.put(1);
    }

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
        Operand x = designator();
        if (startOfAssignop.contains(sym)){
          if (!assignableOperandKinds.contains(x.kind)){
            error(CANNOT_ASSIGN_TO, x.kind.name());
          }
          OpCode calcType = assignop();
          Operand y = expr();
          if (calcType == OpCode.nop) {// assignop '='
            if (y.type == Tab.noType){
              error(INVALID_CALL); // todo change position of error
            }
            if (!y.type.assignableTo(x.type)){
              error(INCOMP_TYPES);
            }
            code.assign(x, y);
          } else {
            compoundAssignment(x, y, calcType);
          }


        } else {
          switch(sym){
            case lpar:
              actPars();
              break;
            case pplus:
              increment(x, 1);
              break;
            case mminus:
              increment(x, -1);
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
          Operand returnVal = expr();
          code.load(returnVal);
          code.put(OpCode.exit);
          code.put(OpCode.return_);
        }
        check(semicolon);
        break;
      case read:
        scan();
        check(lpar);
        x = designator();
        if (x.type == Tab.intType){
          code.put(OpCode.read);
        } else if (x.type == Tab.charType){
          code.put(OpCode.bread);
        } else {
          error(READ_VALUE);
        }
        Operand y = new Operand(Tab.noType); // dummy operator required to call assign
        code.assign(x, y);
        check(rpar);
        check(semicolon);
        break;
      case print:
        scan();
        check(lpar);
        x = expr();
        if (x.type != Tab.intType && x.type != Tab.charType){
          error(PRINT_VALUE);
        }
        code.load(x);
        int width;
        if (sym == comma){
          scan();
          check(number);
          width = t.numVal;
        } else {
          width = 0;
        }
        code.loadConst(width);
        if (x.type == Tab.intType){
          code.put(OpCode.print);
        } else {
          code.put(OpCode.bprint);
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

  private OpCode assignop(){
    OpCode code;
    switch(sym){
      case assign:
        scan();
        code = OpCode.nop;
        break;
      case plusas:
        scan();
        code = OpCode.add;
        break;
      case minusas:
        scan();
        code = OpCode.sub;
        break;
      case timesas:
        scan();
        code = OpCode.mul;
        break;
      case slashas:
        scan();
        code = OpCode.div;
        break;
      case remas:
        scan();
        code = OpCode.rem;
        break;
      default:
        error(ASSIGN_OP);
        code = OpCode.nop;
    }
    return code;
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

  private Operand expr(){
    boolean negate = false;
    if (sym == minus){
      scan();
      negate = true;
    }
    Operand x = term();
    if (negate){
      if (x.type != Tab.intType){
        error(NO_INT_OPERAND);
      }
      if (x.kind == Operand.Kind.Con){
        x.val = -x.val;
      } else {
        code.load(x);
        code.put(OpCode.neg);
      }
    }
    while (sym == plus || sym == minus) {
      OpCode opCode = addop();
      code.load(x);
      Operand y = term();
      if (y.type != Tab.intType){
        error(NO_INT_OPERAND);
      }
      code.load(y);
      code.put(opCode);
    }
    return x;
  }

  private Operand term(){
    Operand x = factor();
    while (true){
      if (sym == times || sym == slash || sym == rem){
        OpCode opCode = mulop();
        code.load(x);
        Operand y = factor();
        code.load(y);
        if (x.type != Tab.intType || y.type != Tab.intType){
          error(NO_INT_OPERAND);
        }
        code.put(opCode);
      } else if (sym == exp){
        scan();
        check(number);
        if (x.type != Tab.intType){
          error(NO_INT_OPERAND);
        }
        code.load(x);
        int exponent = t.numVal;
        if (exponent == 0){
          code.put(OpCode.pop);
          code.put(OpCode.const_1);
        } else {
          for (int i = 0; i < exponent - 1; i++){
            code.put(OpCode.dup);
          }
          for (int i = 0; i < exponent - 1; i++) {
            code.put(OpCode.mul);
          }
        }
      } else {
        break;
      }
    }
    return x;
  }

  private Operand factor(){
    Operand x;
    switch (sym){
      case ident:
        x = designator();
        if (sym == lpar){
          actPars();
        }
        break;
      case number:
        scan();
        x = new Operand(t.numVal);
        break;
      case charConst:
        scan();
        x = new Operand(t.numVal);
        x.type = Tab.charType;
        break;
      case new_:
        scan();
        check(ident);
        Obj o = tab.find(t.val);
        if (o.kind != Obj.Kind.Type){
          error(NO_TYPE);
        }
        if (sym == lbrack){
          scan();
          Operand length = expr();
          if (length.type != Tab.intType){
            error(ARRAY_SIZE);
          }
          code.load(length);
          code.put(OpCode.newarray);
          Struct type = new Struct(o.type);
          x = new Operand(type);
          if (o.type == Tab.charType){
            code.put(0);
          } else {
            code.put(1);
          }
          check(rbrack);
        } else {
          if (o.type == Tab.intType || o.type == Tab.charType){
            error(NO_CLASS_TYPE);
          }
          x = new Operand(o.type);
          code.put(OpCode.new_);
          code.put2(o.type.nrFields());
        }//todo somehow get return value
        break;
      case lpar:
        scan();
        x = expr();
        check(rpar);
        break;
      default:
        error(INVALID_FACT);
        x = new Operand(tab.noObj, this);//todo maybe change
    }
    return x;
  }

  private Operand designator(){
    check(ident);
    Operand x = new Operand(tab.find(t.val), this);
    while (true){
      if (sym == period){
        if (x.type.kind != Struct.Kind.Class){
          error(NO_CLASS);
        }
        scan();
        code.load(x);
        check(ident);
        Obj obj = tab.findField(t.val, x.type);
        x.kind = Operand.Kind.Fld;
        x.type = obj.type;
        x.adr = obj.adr;
      } else if (sym == lbrack) {
        scan();
        code.load(x);
        Operand index = expr();
        if (x.type.kind != Struct.Kind.Arr){
          error(NO_ARRAY);
        }
        if (index.type != Tab.intType){
          error(ARRAY_INDEX);
        }
        code.load(index);
        x.kind = Operand.Kind.Elem;
        x.type = x.type.elemType;
        check(rbrack);
      } else {
        break;
      }
    }
    return x;
  }

  private OpCode addop(){
    OpCode opCode;
    if (sym == plus){
      opCode = OpCode.add;
      scan();
    } else if (sym == minus){
      opCode = OpCode.sub;
      scan();
    } else {
      opCode = OpCode.nop;
      error(ADD_OP);
    }
    return opCode;
  }

  private OpCode mulop(){
    OpCode opCode;
    if (sym == times){
      opCode = OpCode.mul;
      scan();
    } else if (sym == slash){
      opCode = OpCode.div;
      scan();
    } else if (sym == rem){
      scan();
      opCode = OpCode.rem;
    } else {
      error(MUL_OP);
      opCode = OpCode.nop;
    }
    return opCode;
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
  // ------------------------------------

  // Private helper methods
  /**
   * handles ++ and --
   */
  private void increment(Operand x, int n){
    if (x.type != Tab.intType){
      error(NO_INT_OPERAND);
    }
    if (!assignableOperandKinds.contains(x.kind)){
      error(CANNOT_ASSIGN_TO, x.kind.name());
    }
    scan();
    if (x.kind == Operand.Kind.Local){
      //code.compoundAssignmentPrepare(x);
      code.inc(x, n);
    } else {
      compoundAssignment(x, new Operand(n), OpCode.add);
    }
  }

  /**
   * handles +=, -=, *=, /=, %=
   * @param x left side
   * @param y right side
   * @param calcType type of the calculation
   */
  private void compoundAssignment(Operand x, Operand y, OpCode calcType){
    if (x.type != Tab.intType || y.type != Tab.intType){
      error(NO_INT_OPERAND);
    }
    code.compoundAssignmentPrepare(x);
    if (x.kind == Operand.Kind.Elem){
      code.load(y);
      code.put(calcType);
      code.put(OpCode.astore);
    } else if (x.kind == Operand.Kind.Fld){
      code.load(y);
      code.put(calcType);
      code.put(OpCode.putfield);
      code.put2(x.adr);
    } else {
      code.load(y);
      code.put(calcType);
      code.assign(x, new Operand(Tab.noType)); // dummy object, todo change
    }
  }

  // ====================================
  // ====================================
}
