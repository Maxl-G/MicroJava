package ssw.mj.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ssw.mj.Errors.Message;

import static ssw.mj.Errors.Message.*;

/**
 * Test cases for the <code>Parser</code> class.
 */
@Timeout(Configuration.TIMEOUT)
public class RecoverTest extends CompilerTestCaseSupport {

  @Test
  public void wrongGlobalDecl() {
    init("program Test" + LF + //
            "  123;" + LF + //
            "{ void main() { } }");
    expectError(2, 3, INVALID_DECL);
    parseAndVerify();
  }

  @Test
  public void wrongMethDecl1() {
    init("program Test {" + LF + //
            "  void main() { }" + LF + //
            "  program wrong1() { " + LF + //
            "    if (1>2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 3, INVALID_METH_DECL);
    parseAndVerify();
  }

  @Test
  public void wrongMethDecl2() {
    init("program Test {" + LF + //
            "  program wrong1() { " + LF + //
            "    if (1>2);" + LF + //
            "  }" + LF + //
            "  void main() { }" + LF + //
            "  program wrong2() {" + LF + //
            "    if (1>2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(2, 3, INVALID_METH_DECL);
    expectError(6, 3, INVALID_METH_DECL);
    parseAndVerify();
  }

  @Test
  public void wrongMethDecl3() {
    init("program Test {" + LF + //
            "  program wrong1() { }" + LF + //
            "  void main() { }" + LF + //
            "  program wrong2() { }" + LF + //
            "}");
    expectError(2, 3, INVALID_METH_DECL);
    expectError(4, 3, INVALID_METH_DECL);
    parseAndVerify();
  }

  @Test
  public void wrongStat() {
    init("program Test {" + LF + //
            "  void main() {  " + LF + //
            "    123;" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 5, INVALID_STAT);
    parseAndVerify();
  }

  @Test
  public void multipleErrors() {
    init("program Test " + LF + //
            "  int x" + LF + //
            "{" + LF + //
            "  void main( {" + LF + //
            "    if (1 x 2);" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 1, TOKEN_EXPECTED, ";");
    expectError(4, 14, TOKEN_EXPECTED, ")");
    expectError(5, 11, REL_OP);
    parseAndVerify();
  }

  // ---- multiple errors & recovery
  @Test
  public void noRecover1() {
    init("program Test {" + LF + //
            "  void main this method will never recover");

    expectError(2, 13, TOKEN_EXPECTED, "(");
    parseAndVerify();
  }

  @Test
  public void noRecover2() {
    init("program Test {" + LF + //
            "  void main() {  " + LF + //
            "    if this method will never recover");

    expectError(3, 8, TOKEN_EXPECTED, "(");
    parseAndVerify();
  }

  @Test
  public void recoverDecl1() {
    init("program Test" + LF + //
            "  int i1, if" + LF + //
            "  in i2;" + LF + //
            "  final int i3 = 0;" + LF + //
            "{" + LF + //
            "  void main() {  " + LF + //
            "    if (i1 < i3);" + LF + //
            "  }" + LF + //
            "}");

    expectError(2, 11, TOKEN_EXPECTED, "identifier");
    parseAndVerify();
  }

  @Test
  public void recoverDecl2() {
    init("program Test" + LF + //
            "  int i1, if" + LF + //
            "  in i2;" + LF + //
            "  int i3;" + LF + //
            "{" + LF + //
            "  void main() {  " + LF + //
            "    if (i1 < i3);" + LF + //
            "  }" + LF + //
            "}");

    expectError(2, 11, TOKEN_EXPECTED, "identifier");
    parseAndVerify();
  }

  @Test
  public void recoverStat() {
    init("program Test {" + LF + //
            "  void main() {  " + LF + //
            "    567 since distance stays too small no follow up errors here;" + LF + //
            "    if (1 < 2);" + LF + //
            "    if (1 x 2);" + LF + //
            "  }" + LF + //
            "}");

    expectError(3, 5, INVALID_STAT);
    expectError(5, 11, REL_OP);
    parseAndVerify();
  }

  @Test
  public void resetErrDist() {
    init("program Test {" + LF + //
            "  void main() {" + LF + //
            "    if () if () if();" + LF + //
            "  }" + LF + //
            "}");
    expectError(3, 9, INVALID_FACT);
    expectError(3, 15, INVALID_FACT);
    expectError(3, 20, INVALID_FACT);
    parseAndVerify();
  }

  @Test
  public void illegalMethodStart() {
    init("program Test" + LF + // 1
            "{" + LF + // 2
            "  void foo()" + LF + // 3
            "  void foo(char x) { }" + LF + // 4
            "  void main() { }" + LF + // 5
            "}" + LF // 6
    );
    expectError(4, 3, Message.TOKEN_EXPECTED, "{");
    parseAndVerify();
  }
}
