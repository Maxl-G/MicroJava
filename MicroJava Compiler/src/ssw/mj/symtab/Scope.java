package ssw.mj.symtab;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MicroJava Symbol Table Scopes
 */
public final class Scope {
  /**
   * Reference to enclosing scope.
   */
  private Scope outer;
  /**
   * Declarations of this scope.
   */
  private Map<String, Obj> locals = new LinkedHashMap<>();
  /**
   * Number of variables in this scope.
   */
  private int nVars;

  public Scope(Scope outer) {
    this.outer = outer;
  }

  public int nVars() {
    return nVars;
  }

  public Obj findGlobal(String name) {
    Obj res = findLocal(name);
    if (res == null && outer != null) {
      res = outer.findGlobal(name);
    }
    return res;
  }

  public Obj findLocal(String name) {
    return locals.get(name);
  }

  public void insert(Obj o) {
    locals.put(o.name, o);
    if (o.kind == Obj.Kind.Var) {
      nVars++;
    }
  }

  public Scope outer() {
    return outer;
  }

  public Map<String, Obj> locals() {
    return Collections.unmodifiableMap(locals);
  }
}
