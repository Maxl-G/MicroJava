package ssw.mj.impl;

import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Scope;
import ssw.mj.symtab.Struct;

import static ssw.mj.Errors.Message.*;

public final class Tab {

  // Universe
  public static final Struct noType = new Struct(Struct.Kind.None);
  public static final Struct intType = new Struct(Struct.Kind.Int);
  public static final Struct charType = new Struct(Struct.Kind.Char);
  public static final Struct nullType = new Struct(Struct.Kind.Class);

  // TODO Exercise 4: Assign universe objects in constructor
  public final Obj noObj = null, chrObj = null, ordObj = null, lenObj = null;

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

    // TODO Exercise 4: set up "universe" (= predefined names)
    // opening scope (curLevel goes to -1, which is the universe level)
    openScope();
    // TODO Exercise 4 ...
    closeScope();
  }

  // ===============================================
  // TODO Exercise 4: implementation of symbol table
  // ===============================================

  public void openScope() {
    // TODO Exercise 4
  }

  public void closeScope() {
    // TODO Exercise 4
  }

  public Obj insert(Obj.Kind kind, String name, Struct type) {
    // TODO Exercise 4
    return null;
  }

  /**
   * Retrieves the object with <code>name</code> from the innermost scope.
   */
  public Obj find(String name) {
    // TODO Exercise 4
    return null;
  }

  /**
   * Retrieves the field <code>name</code> from the fields of
   * <code>type</code>.
   */
  public Obj findField(String name, Struct type) {
    // TODO Exercise 4
    return null;
  }

  // ===============================================
  // ===============================================
}
