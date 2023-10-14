package ssw.mj.codegen;

import ssw.mj.impl.Code;
import ssw.mj.impl.Code.OpCode;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class Decoder {
  private byte[] codeBuf; // code buffer
  private int cur; // address of next byte to decode
  private int adr; // address of currently decoded instruction

  private int getAndMove() {
    return codeBuf[cur++];
  }

  private int getAndMove2() {
    return (getAndMove() << 8) + (getAndMove() & 0xFF);
  }

  private int getAndMove4() {
    return (getAndMove2() << 16) + (getAndMove2() & 0xFFFF);
  }

  private String jumpDist() {
    int dist = getAndMove2();
    int pos = adr + dist;
    return dist + " (=" + pos + ")";
  }

  public String decode(Code code) {
    return decode(code.buf, 0, code.pc);
  }

  public String decode(byte[] buf, int off, int len) {
    StringBuilder sb = new StringBuilder();
    codeBuf = buf;
    cur = off;
    adr = cur;
    while (cur < len) {
      sb.append(adr);
      sb.append(": ");
      sb.append(decode(OpCode.get(getAndMove())));
      sb.append("\n");
      adr = cur;
    }
    return sb.toString();
  }

  private String decode(OpCode opCode) {
    if (opCode == null) {
      return "--error, unknown opcode--";
    }

    return switch (opCode) {
      // Operations without parameters in the code buffer
      case load_0, load_1, load_2, load_3, store_0, store_1, store_2, store_3, const_0, const_1, const_2, const_3, const_4, const_5, const_m1, add, sub, mul, div, rem, neg, shl, shr, aload, astore, baload, bastore, arraylength, pop, dup, dup2, exit, return_, read, print, bread, bprint ->
              opCode.cleanName();
      // Operations with one 1 byte parameter in the code buffer
      case load, store, newarray, trap -> opCode.cleanName() + " " + getAndMove();
      // Operations with one 2 byte parameter in the code buffer
      case getstatic, putstatic, getfield, putfield, new_ -> opCode.cleanName() + " " + getAndMove2();
      // Operations with one 4 byte parameter in the code buffer
      case const_ -> opCode.cleanName() + " " + getAndMove4();
      // Operations with two 1 byte parameters in the code buffer
      case inc, enter -> opCode.cleanName() + " " + getAndMove() + ", " + getAndMove();
      // Operations with a jump distance as a parameter in the code buffer
      case jmp, jeq, jne, jlt, jle, jgt, jge, call -> opCode.cleanName() + " " + jumpDist();
      default -> "--error--";
    };
  }

  public void decodeFile(String filename) throws IOException {
    DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(filename)));
    byte[] sig = new byte[2];
    in.read(sig, 0, 2);
    System.out.println("" + (char) sig[0] + (char) sig[1]);
    int codeSize = in.readInt();
    System.out.println("codesize = " + codeSize);
    System.out.println("datasize = " + in.readInt());
    System.out.println("startPC  = " + in.readInt());
    byte[] code = new byte[codeSize];
    in.read(code);
    System.out.println(decode(code, 0, codeSize));
    in.close();
  }

  public static void main(String[] args) throws IOException {
    if (args.length > 0) {
      Decoder dec = new Decoder();
      dec.decodeFile(args[0]);
    }
  }
}
