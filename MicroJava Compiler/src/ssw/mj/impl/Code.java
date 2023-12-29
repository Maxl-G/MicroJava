package ssw.mj.impl;

import ssw.mj.codegen.Label;
import ssw.mj.codegen.Operand;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.EnumSet;

import static ssw.mj.Errors.Message.*;
import static ssw.mj.Errors.Message.NO_INT_OPERAND;

public final class Code {

  public enum OpCode {
    load,
    load_0,
    load_1,
    load_2,
    load_3,
    store,
    store_0,
    store_1,
    store_2,
    store_3,
    getstatic,
    putstatic,
    getfield,
    putfield,
    const_0,
    const_1,
    const_2,
    const_3,
    const_4,
    const_5,
    const_m1,
    const_,
    add,
    sub,
    mul,
    div,
    rem,
    neg,
    shl,
    shr,
    inc,
    new_,
    newarray,
    aload,
    astore,
    baload,
    bastore,
    arraylength,
    pop,
    dup,
    dup2,
    jmp,
    jeq,
    jne,
    jlt,
    jle,
    jgt,
    jge,
    call,
    return_,
    enter,
    exit,
    read,
    print,
    bread,
    bprint,
    trap,
    nop;

    public int code() {
      return ordinal() + 1;
    }

    public String cleanName() {
      String name = name();
      if (name.endsWith("_")) {
        name = name.substring(0, name.length() - 1);
      }
      return name;
    }

    public static OpCode get(int code) {
      if (code < 1 || code > values().length) {
        return null;
      }
      return values()[code - 1];
    }
  }

  public enum CompOp {
    eq, ne, lt, le, gt, ge;

    public static CompOp invert(CompOp op) {
      if (op == null) {
        throw new IllegalArgumentException("Compare operator must not be null!");
      }
      switch (op) {
        case eq:
          return ne;
        case ne:
          return eq;
        case lt:
          return ge;
        case le:
          return gt;
        case gt:
          return le;
        case ge:
          return lt;
        default:
          // Cannot happen, we covered all six compare operations as well as null parameter
          // This is purely to prevent the compiler from complaining about a missing return statement
          throw new IllegalArgumentException("Impossible compare operator");
      }
    }

    public static OpCode toOpCode(CompOp op) {
      switch (op) {
        case eq:
          return OpCode.jeq;
        case ge:
          return OpCode.jge;
        case gt:
          return OpCode.jgt;
        case le:
          return OpCode.jle;
        case lt:
          return OpCode.jlt;
        case ne:
          return OpCode.jne;
      }
      return OpCode.nop;
    }
  }

  /**
   * Code buffer
   */
  public byte[] buf;

  /**
   * Program counter. Indicates next free byte in code buffer.
   */
  public int pc;

  /**
   * PC of main method (set by parser).
   */
  public int mainpc;

  /**
   * Length of static data in words (set by parser).
   */
  public int dataSize;

  /**
   * According parser.
   */
  private final Parser parser;

  // ----- initialization

  public Code(Parser p) {
    parser = p;
    buf = new byte[100];
    pc = 0;
    mainpc = -1;
    dataSize = 0;
  }

  // ----- code storage management

  public void put(OpCode code) {
    put(code.code());
  }

  public void put(int x) {
    if (pc == buf.length) {
      buf = Arrays.copyOf(buf, buf.length * 2);
    }
    buf[pc++] = (byte) x;
  }

  public void put2(int x) {
    put(x >> 8);
    put(x);
  }

  public void put4(int x) {
    put2(x >> 16);
    put2(x);
  }

  public void put2(int pos, int x) {
    int oldpc = pc;
    pc = pos;
    put2(x);
    pc = oldpc;
  }

  public int get(int pos) {
    return buf[pos];
  }

  /**
   * Write the code buffer to the output stream.
   */
  public void write(OutputStream os) throws IOException {
    int codeSize = pc;

    ByteArrayOutputStream header = new ByteArrayOutputStream();
    DataOutputStream headerWriter = new DataOutputStream(header);
    headerWriter.writeByte('M');
    headerWriter.writeByte('J');
    headerWriter.writeInt(codeSize);
    headerWriter.writeInt(dataSize);
    headerWriter.writeInt(mainpc);
    headerWriter.close();

    os.write(header.toByteArray());

    os.write(buf, 0, codeSize);
    os.flush();
    os.close();
  }

  // ======================================================
  // TODO Exercise 5-6: implementation of code generation
  // ======================================================

  // Various code generation methods such as load or assign

  /**
   * Load the operand x onto the expression stack.
   */
  public void load(Operand x) {
    switch (x.kind){
      case Con:
        loadConst(x.val);
        break;
      case Local:
        switch (x.adr){
          case 0 -> put(OpCode.load_0);
          case 1 -> put(OpCode.load_1);
          case 2 -> put(OpCode.load_2);
          case 3 -> put(OpCode.load_3);
          default -> {
            put(OpCode.load);
            put(x.adr);
          }
        }
        break;
      case Static:
        put(OpCode.getstatic);
        put2(x.adr);
        break;
      case Stack:
        break;
      case Fld:
        put(OpCode.getfield);
        put2(x.adr);
        break;
      case Elem:
        if (x.type == Tab.charType){
          put(OpCode.baload);
        } else {
          put(OpCode.aload);
        }
        break;
      default:
        parser.error(NO_VAL);
    }
    x.kind = Operand.Kind.Stack;
  }

  /**
   * Generate an increment instruction that increments x by n.
   */
  public void inc(Operand x, int n) {
    if (x.kind == Operand.Kind.Local && -128 <= n && n < 128){
      put(OpCode.inc);
      put(x.adr);
      put(n);
    } else {

      assign(x, new Operand(n));
    }
  }

  /**
   * Generate an assignment x = y.
   */
  public void assign(Operand x, Operand y) {
    load(y);
    switch (x.kind) {
      case Local:
        switch (x.adr) {
          case 0 -> put(OpCode.store_0);
          case 1 -> put(OpCode.store_1);
          case 2 -> put(OpCode.store_2);
          case 3 -> put(OpCode.store_3);
          default -> {
            put(OpCode.store);
            put(x.adr);
          }
        }
        break;
      case Static:
        put(OpCode.putstatic);
        put2(x.adr);
        break;
      case Fld:
        put(OpCode.putfield);
        put2(x.adr);
        break;
      case Elem:
        if (x.type == Tab.charType) {
          put(OpCode.bastore);
        }
        else {
          put(OpCode.astore);
        }
        break;
      default: parser.error(NO_VAL);
    }

  }

  /**
   * Load an integer constant onto the expression stack.
   */
  public void loadConst(int n) {
    switch (n){
      case -1 -> put(OpCode.const_m1);
      case 0 -> put(OpCode.const_0);
      case 1 -> put(OpCode.const_1);
      case 2 -> put(OpCode.const_2);
      case 3 -> put(OpCode.const_3);
      case 4 -> put(OpCode.const_4);
      case 5 -> put(OpCode.const_5);
      default -> {
        put(OpCode.const_);
        put4(n);
      }
    }
  }

  /**
   * Prepares a compound assignment.
   */
  public void compoundAssignmentPrepare(Operand x) {
    Operand.Kind kindBeforeLoad = x.kind;

    // Field accesses (such as x.y) or array accesses (such as arr[2]) on the left-hand side of
    // an compound assignment (e.g., arr[2] += 4) need to correctly use dup or dup2. Implement here.
    if (x.kind == Operand.Kind.Fld){
      put(OpCode.dup);
      load(x);
    } else if (x.kind == Operand.Kind.Elem){
      put(OpCode.dup2);
      load(x);
    } else {
      load(x);
    }
    // Do not switch kind to Stack after loading x.
    // We still need its type later on during the assign().
    x.kind = kindBeforeLoad;
  }

  // --------------------

  public void methodCall(Operand x) {
    // TODO Exercise 6
  }

  /**
   * Unconditional jump.
   */
  public void jump(Label lab) {
    // TODO Exercise 6
  }

  /**
   * True Jump. Generates conditional jump instruction and links it to true
   * jump chain.
   */
  public void tJump(CompOp op, Label to) {
    // TODO Exercise 6
  }

  /**
   * False Jump. Generates conditional jump instruction and links it to false
   * jump chain.
   */
  public void fJump(CompOp op, Label to) {
    // TODO Exercise 6
  }

  // =================================================
  // =================================================
}
