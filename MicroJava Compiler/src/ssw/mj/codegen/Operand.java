package ssw.mj.codegen;

import ssw.mj.impl.Code;
import ssw.mj.impl.Code.CompOp;
import ssw.mj.impl.Parser;
import ssw.mj.impl.Tab;
import ssw.mj.symtab.Obj;
import ssw.mj.symtab.Struct;

import java.util.EnumSet;

import static ssw.mj.Errors.Message.NO_OPERAND;

public class Operand {
  /**
   * Possible operands.
   */
  public enum Kind {
    Con, Local, Static, Stack, Fld, Elem, Meth, Cond, None
  }

  /**
   * Kind of the operand.
   */
  public Kind kind;
  /**
   * The type of the operand (reference to symbol table).
   */
  public Struct type;
  /**
   * Only for Con: Value of the constant.
   */
  public int val;
  /**
   * Only for Local, Static, Fld, Meth: Offset of the element.
   */
  public int adr;
  /**
   * Only for Cond: Relational operator.
   */
  public CompOp op;
  /**
   * Only for Meth: Method object from the symbol table.
   */
  public Obj obj;
  /**
   * Only for Cond: Target for true jumps.
   */
  public Label tLabel;
  /**
   * Only for Cond: Target for false jumps.
   */
  public Label fLabel;

  /**
   * Constructor for named objects: constants, variables, methods
   */
  public Operand(Obj o, Parser parser) {
    type = o.type;
    val = o.val;
    adr = o.adr;
    switch (o.kind) {
      case Con:
        kind = Kind.Con;
        break;
      case Var:
        if (o.level == 0) {
          kind = Kind.Static;
        } else {
          kind = Kind.Local;
        }
        break;
      case Meth:
        kind = Kind.Meth;
        obj = o;
        break;
      default:
        kind = Kind.None;
        parser.error(NO_OPERAND);
    }
  }

  /**
   * Constructor for compare operations
   */
  public Operand(CompOp op, Code code) {
    this(code);
    this.kind = Kind.Cond;
    this.op = op;
  }

  public Operand(Code code) {
    tLabel = new Label(code);
    fLabel = new Label(code);
  }

  /**
   * Constructor for stack operands
   */
  public Operand(Struct type) {
    this.kind = Kind.Stack;
    this.type = type;
  }

  /**
   * Constructor for integer constants
   */
  public Operand(int x) {
    kind = Kind.Con;
    type = Tab.intType;
    val = x;
  }

  public boolean canBeAssignedTo() {
    return EnumSet.of(Kind.Local, Kind.Static, Kind.Fld, Kind.Elem).contains(kind);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Op[");
    switch (kind) {
      case Con:
        sb.append(type).append(' ');
        sb.append(val);
        break;
      case Local:
      case Static:
      case Fld:
        sb.append(kind).append(' ');
        sb.append(type).append(' ');
        sb.append(adr);
        break;
      case Cond:
        sb.append(op);
        break;
      case Meth:
        sb.append(obj);
        break;
      case Elem:
      case Stack:
        sb.append(kind).append(' ');
        sb.append(type);
        break;
    }
    return sb.append(']').toString();
  }

}
