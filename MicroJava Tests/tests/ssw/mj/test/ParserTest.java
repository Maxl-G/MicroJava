package ssw.mj.test;

import static ssw.mj.Errors.Message.*;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Timeout;
import ssw.mj.scanner.Token;

@Timeout(Configuration.TIMEOUT)
public class ParserTest extends CompilerTestCaseSupport {

  // ---- Working test cases

  @Test
  public void testWorkingFinalDecls() {
    init("program Test" + LF + // 1
            "  final int i = 1;" + LF + // 2
            "  final int j = 1;" + LF + // 3
            "  final int k = 1;" + LF + // 4
            "{ void main() { } }"); // 5
    parseAndVerify();
  }

  @Test
  public void testWorkingDecls() {
    init("program Test" + LF + // 1
            "  int i;" + LF + // 2
            "  int j, k;" + LF + // 3
            "{ void main() { } }"); // 4
    parseAndVerify();
  }

  @Test
  public void testWorkingMethods() {
    init("program Test" + LF + // 1
            "  int i;" + LF + // 2
            "  int j, k;" + LF + // 3
            "{" + LF + // 4
            " void foo() { }" + LF + // 5
            " void bar() { }" + LF + // 6
            " void main() { }" + LF + // 7
            " }" + LF // 8
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingMethodsWithParameters() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(int i) { }" + LF + // 3
            " void bar(int i, char c) { }" + LF + // 4
            " void main() { }" + LF + // 5
            " }" + LF // 6
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingMethodsWithLocals() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo() int i; { }" + LF + // 3
            " void bar() int i; char c; { }" + LF + // 4
            " void main() { }" + LF + // 5
            " }" + LF // 6
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingMethodsWithParametersAndLocals() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch) int i; { }" + LF + // 3
            " void bar(int x, int y) int i; char c; { }" + LF + // 4
            " void main() { }" + LF + // 5
            " }" + LF // 6
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingMethodCall() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch) int i; { }" + LF + // 3
            " void main() { foo('a'); }" + LF + // 4
            " }" + LF // 5
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingMethodCallTwoParams() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch, int x) int i; { }" + LF + // 3
            " void main() { foo('a', 1); }" + LF + // 4
            " }" + LF // 5
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingMethodCallThreeParams() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void foo(char ch, int x, char ch2) int i; { }" + LF + // 3
            " void main() { foo('a', 1, 'b'); }" + LF + // 4
            " }" + LF // 5
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingClass() {
    init("program Test" + LF + // 1
            "class X { int i; int j; }" + LF + // 2
            "{" + LF + // 3
            " void main() X x; { x = new X; }" + LF + // 4
            "}" + LF // 5
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingArray() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            "  void main() int[] x; { x = new int[10]; }" + LF + // 3
            "}" + LF // 4
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingIncDec() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            " void main() int i; { i--; i++; }" + LF + // 3
            " }" + LF // 4
    );
    parseAndVerify();
  }

  @Test
  public void testWorkingElseIf() {
    init("program Test {" + LF + // 1
            "  void main() int i; {" + LF + // 2
            "    if (i > 10) i++;" + LF + // 3
            "    else if (i < 5) i--;" + LF + // 4
            "    else i += 8;" + LF + // 5
            "  }" + LF + // 6
            "}");
    parseAndVerify();
  }

  @Test
  public void testWorkingLoop() {
    init("program Test {" + LF + // 1
            "  void main () int i; {" + LF + // 2
            "    i = 0;" + LF + // 3
            "    while (i < 42) {" + LF + // 4
            "	   i++;" + LF + // 5
            "    }" + LF + // 6
            "  }" + LF + // 7
            "}");
    parseAndVerify();
  }


  // ---- Errors in Parser.java

  @Test
  public void wrongStart() {
    init("noprogram Test { }");
    expectError(1, 1, TOKEN_EXPECTED, "program");
    parseAndVerify();
  }

  @Test
  public void noProgName() {
    init("program { }");
    expectError(1, 9, TOKEN_EXPECTED, "identifier");
    parseAndVerify();
  }

  @Test
  public void wrongVarDecl() {
    init("program Test " + LF + //
            "int var1,,,,var2;" + LF + //
            "{ void main() { } }");
    expectError(2, 10, TOKEN_EXPECTED, Token.Kind.ident.label());
    parseAndVerify();
  }

  @Test
  public void wrongConstDecl() {
    init("program Test" + LF + //
            "  final int i = a;" + LF + //
            "{ void main() { } }");
    expectError(2, 17, CONST_DECL);
    parseAndVerify();
  }

  @Test
  public void wrongDesignFollow() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i**;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 6, DESIGN_FOLLOW);
    parseAndVerify();
  }

  @Test
  public void wrongFactor() {
    init("program Test {" + LF + //
            "  void main () int i; {  " + LF + //
            "    i = i + if;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 13, INVALID_FACT);
    parseAndVerify();
  }

  @Test
  public void wrongRelOp() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    if (i x 5);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 11, REL_OP);
    parseAndVerify();
  }

  @Test
  public void exp() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 2 ** 5;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void mulAssign() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 2;" + LF + //
            "    i *= 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void noExpAssign() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 2;" + LF + //
            "    i **= 2;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 7, DESIGN_FOLLOW);
    parseAndVerify();
  }

  @Test
  public void noNumberExponent() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 3;" + LF + //
            "    i = 2 ** i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(4, 14, TOKEN_EXPECTED, Token.Kind.number.label());
    parseAndVerify();
  }

  @Test
  public void numberExponent() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 3;" + LF + //
            "    i = i ** 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void constantBase() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 4 ** 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void constantBaseNonConstantExp() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 4 ** i;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 14, TOKEN_EXPECTED, Token.Kind.number.label());
    parseAndVerify();
  }

  @Test
  public void longTerm() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 4 + 5 + 2 ** 3 * (1 * (1 + 1) / 2) ** 1 - 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void longTermWithVar() {
    init("program Test {" + LF + //
            "  void main() int i; {" + LF + //
            "    i = 2;" + LF + //
            "    i = 4 + 5 + i ** 3 * (1 * (1 + 1) / i) ** 1 - 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void arrBase() {
    init("program Test {" + LF + //
            "  int calc() { return 2; }" + LF + // 3
            "  void main() int i; int[] arr; {" + LF + //
            "    arr = new int[2];" + LF + //
            "    arr[0] = 1; arr[1] = 2;" + LF + //
            "    i = arr[1] ** 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void fieldBase() {
    init("program Test " + LF + //
            "  class C2 { int x; }" + LF + // 3
            "  class C { C2 c2; } {" + LF + // 3
            "  void main() int i; C c; {" + LF + //
            "    c = new C;" + LF + //
            "    c.c2 = new C2;" + LF + //
            "    c.c2.x = 2;" + LF + //
            "    i = c.c2.x ** 2;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void returnExpr() {
    init("program Test {" + LF + //
            "  void main() { }" + LF + //
            "  int wrong1() { " + LF + //
            "    return 2 + 3;" + LF + //
            "  }" + LF + //
            "}");
    parseAndVerify();
  }

  @Test
  public void eofExpected1() {
    init("program Test {" + LF + //
            "  void main() {}" + LF + //
            "}moretext");
    expectError(3, 2, TOKEN_EXPECTED, "end of file");
    parseAndVerify();
  }

  @Test
  public void invalidEOF1() {
    init("program Test {" + LF + //
            "  void main() {");
    expectError(2, 16, TOKEN_EXPECTED, "}");
    parseAndVerify();
  }

  @Test
  public void invalidEOF2() {
    init("program Test {" + LF + //
            "  void main() {" + LF + //
            "    if ()");
    expectError(3, 9, INVALID_FACT);
    parseAndVerify();
  }

  @Test
  public void invalidEOF3() {
    init("program Test" + LF + //
            "  class C {" + LF + //
            "    int i");
    expectError(3, 10, TOKEN_EXPECTED, ";");
    parseAndVerify();
  }
}
