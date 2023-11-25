package ssw.mj.impl;

import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Struct;

import java.util.Map;

import static ssw.mj.Errors.Message.*;

public final class Tab {

  // Universe
  public static final Struct noType = new Struct(Struct.Kind.None);
  public static final Struct intType = new Struct(Struct.Kind.Int);
  public static final Struct charType = new Struct(Struct.Kind.Char);
  public static final Struct nullType = new Struct(Struct.Kind.Class);

  public final Obj noObj, chrObj, ordObj, lenObj;

  /**
   * Only used for reporting errors.
   */
  private final Parser parser;
  /**
   * The current top scope.
   */
  public Scope curScope = null;
  // First scope opening (universe) will increase this to -1
  /**
   * Nesting level of current scope.
   */
  private int curLevel = -2;

  public Tab(Parser p) {
    parser = p;
    noObj = new Obj(Obj.Kind.Var, "noObj", noType);

    // opening scope (curLevel goes to -1, which is the universe level)
    openScope();

    // set up "universe" (= predefined names)
    insert(Obj.Kind.Type, "int", intType);
    insert(Obj.Kind.Type, "char", charType);
    insert(Obj.Kind.Con, "null", nullType);

    chrObj = insert(Obj.Kind.Meth, "chr", charType);
    openScope();
    openScope();
    insert(Obj.Kind.Var, "i", intType);
    chrObj.locals = curScope.locals();
    chrObj.nPars = curScope.nVars();
    closeScope();
    closeScope();

    ordObj = insert(Obj.Kind.Meth, "ord", intType);
    openScope();
    openScope();
    insert(Obj.Kind.Var, "ch", charType);
    ordObj.locals = curScope.locals();
    ordObj.nPars = curScope.nVars();
    closeScope();
    closeScope();

    lenObj = insert(Obj.Kind.Meth, "len", intType);
    openScope();
    openScope();
    insert(Obj.Kind.Var, "arr", new Struct(noType));
    lenObj.locals = curScope.locals();
    lenObj.nPars = curScope.nVars();
    closeScope();
    closeScope();
  }

  // ===============================================
  // implementation of symbol table
  // ===============================================

  public void openScope() {
    curScope = new Scope(curScope);
    curLevel++;
  }

  public void closeScope() {
    curScope = curScope.outer();
    curLevel--;
  }

  public Obj insert(Obj.Kind kind, String name, Struct type) {
    Obj obj = new Obj(kind, name, type);
    if (kind == Obj.Kind.Var){
      obj.adr = curScope.nVars();
      obj.level = curLevel;
    }

    if (curScope.findLocal(name) != null) {
      parser.error(DECL_NAME, name);
    }
    curScope.insert(obj);

    return obj;
  }

  /**
   * Retrieves the object with <code>name</code> from the innermost scope.
   */
  public Obj find(String name) {
    Obj o = curScope.findGlobal(name);
    if (o == null){
      parser.error(NOT_FOUND, name);
      return noObj;
    }
    return o;
  }

  /**
   * Retrieves the field <code>name</code> from the fields of
   * <code>type</code>.
   */
  public Obj findField(String name, Struct type) {
    Obj field = type.findField(name);
    if (field == null){
      parser.error(NO_FIELD, name);
      return noObj;
    }
    return field;
  }

  // ===============================================
  // ===============================================
}
