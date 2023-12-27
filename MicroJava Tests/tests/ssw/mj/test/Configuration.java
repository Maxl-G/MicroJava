package ssw.mj.test;

import ssw.mj.codegen.Decoder;

public class Configuration {
  /**
   * set to true to print expected and actual values of all testcases,
   * not only failing ones (prints expected errors, tokens, symbol table, code)
   */
  public static final boolean ALSO_PRINT_SUCCESSFUL_TESTCASES = true;// Boolean.getBoolean("microjava.testcaseOutput");

  /**
   * Set to true to print debug information of the interpreter. Equal to
   * "-debug" on the command line. <br>
   * Remark:<br>
   * This is a lot of output, some test cases might timeout, e.g.
   * CodeGenerationTest.fib
   */
  public static final boolean PRINT_INTERPRETER_DEBUG_OUTPUT = true;//Boolean.getBoolean("microjava.interpreterOutput");

  /**
   * Determines the timeout after which a test case should fail automatically.
   * Default: 10 seconds. The default should work for all test cases
   * on most machines.<br>
   * <em>Attention</em>: For most computers it is likely that there is an
   * endless loop in the MicroJava compiler if a test fails for a timeout.
   */
  public static final long TIMEOUT = 10;

  /**
   * This flag is used by the lecturers to generate the reference solutions for
   * the bytecodes generated during code generation.
   * Students should not change this flag, it should stay false for the whole course.
   */
  public static final boolean GENERATE_REFERENCE_BYTE_CODE = false;
}
