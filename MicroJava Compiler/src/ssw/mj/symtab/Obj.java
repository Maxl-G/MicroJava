package ssw.mj.symtab;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * MicroJava Symbol Table Objects: Every named object in a program is stored in
 * an <code>Obj</code> node. Every scope has a list of objects declared within
 * it.
 */
public class Obj {
  /**
   * Possible codes for object kinds.
   */
  public enum Kind {
    Con, Var, Type, Meth, Prog
  }

  /**
   * Kind of the object node.
   */
  public final Kind kind;

  /**
   * Name of the object node.
   */
  public final String name;

  /**
   * Type of the object node.
   */
  public final Struct type;

  /**
   * Only for Con: Value of the constant.
   */
  public int val;

  /**
   * Only for Var, Meth: Offset of the element.
   */
  public int adr;

  /**
   * Only for Var: Declaration level (0..global, 1..local)
   */
  public int level;

  /**
   * Only for Meth: Number of parameters.
   */
  public int nPars;

  // This is a Collections.emptyMap() on purpose, do not change this line
  // If you finished reading the locals of an method, use meth.locals = curScope.locals() and close the scope afterwards
  /**
   * Only for Meth / Prog: List of local variables / global declarations.
   */
  public Map<String, Obj> locals = Collections.emptyMap();

  public Obj(Kind kind, String name, Struct type) {
    this.kind = kind;
    this.name = name;
    this.type = type;
  }

  @Override
  public String toString() {
    StringBuilder sb;
    switch (kind) {
      case Prog:
        sb = new StringBuilder();
        sb.append(kind).append(' ');
        sb.append(name);
        return sb.toString();
      case Con:
        sb = new StringBuilder();
        sb.append("const ");
        sb.append(type).append(' ');
        sb.append(name).append('=');
        sb.append(renderValue());
        return sb.toString();
      case Var:
        sb = new StringBuilder();
        sb.append(renderVarPrefix()).append(' ');
        sb.append(type).append(' ');
        sb.append(name).append('@');
        renderAddress(sb);
        return sb.toString();
      case Meth:
        sb = new StringBuilder(type.toString()).append(' ');
        sb.append(name).append('(');
        boolean first = true;
        Iterator<Entry<String, Obj>> it = locals.entrySet().iterator();
        for (int i = 0; i < nPars; i++) {
          if (!first) {
            sb.append(", ");
          }
          Entry<String, Obj> e = it.next();
          sb.append(e.getValue().type).append(' ').append(e.getKey());
          first = false;
        }
        sb.append(')').append('@');
        renderAddress(sb);
        return sb.toString();
      case Type:
        sb = new StringBuilder();
        sb.append("Type ");
        sb.append(type).append(' ');
        sb.append(name);
        return sb.toString();
    }
    throw new RuntimeException("Unknown Obj " + kind);
  }

  private void renderAddress(StringBuilder sb) {
    if (kind == Kind.Meth || level == 0) {
      sb.append("0x").append(Integer.toHexString(adr));
    } else {
      sb.append(adr);
    }
  }

  private String renderVarPrefix() {
    if (level == 0) {
      return "global";
    }
    return "local";
  }

  private String renderValue() {
    if (type.kind == Struct.Kind.Char) {
      return Character.toString((char) val);
    }
    return Integer.toString(val);
  }
}
