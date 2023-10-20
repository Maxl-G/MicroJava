package ssw.mj.symtab;

import java.io.Serial;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class Errors {
  public static class PanicMode extends Error {
    @Serial
    private static final long serialVersionUID = 1L;
    // Nothing to implement here.
  }

  public enum Message {
    // ----- error messages first used in ScannerTest
    EMPTY_CHARCONST("empty character constant"),
    UNDEFINED_ESCAPE("undefined escape character sequence ''\\{0}''"),
    MISSING_QUOTE("missing '' at end of character constant"),
    INVALID_CHAR("invalid character {0}"),
    BIG_NUM("{0} too big for integer constant"),
    EOF_IN_COMMENT("unexpected end of file in comment"),
    EOF_IN_CHAR("unexpected end of file in char"),
    ILLEGAL_LINE_END("illegal line end in character constant"),


    // ----- error messages first used in ParserTest
    ADD_OP("additive operator (+ or -) expected"), // cannot occur in current grammar version, but still must be placed correctly
    ASSIGN_OP("assignment operator (=, +=, -=, *=, /=, %=) expected"), // cannot occur in current grammar version, but still must be placed correctly
    CONST_DECL("number or character constant expected"),
    DESIGN_FOLLOW("assignment, method call, increment (++) or decrement (--) expected"),
    INVALID_FACT("invalid start of factor: identifier, number, character constant, new or ( expected"),
    MUL_OP("multiplicative operator (*, /, %) expected"), // cannot occur in current grammar version, but still must be placed correctly
    REL_OP("relational operator (==, !=, >, >=, <, <=) expected"),
    TOKEN_EXPECTED("{0} expected"),

    // ----- error messages first used in RecoverTest
    INVALID_DECL("invalid global declaration"),
    INVALID_STAT("invalid start of statement: identifier, if, while, do, break, return, read, print, '{' or ; expected"),
    INVALID_METH_DECL("invalid start of method decl: type name or void expected"),

    // ----- error messages first used in SymbolTableTest
    CONST_TYPE("value does not match constant type"),
    DECL_NAME("{0} already declared"),
    MAIN_WITH_PARAMS("main method must not have any parameters"),
    MAIN_NOT_VOID("main method must return void"),
    NO_FIELD("{0} is not a field"), // moves from SimpleCodeGenerationTest to SymbolTableTest in 2024
    NO_TYPE("type expected"),
    NOT_FOUND("{0} not found"),
    TOO_MANY_FIELDS("too many fields"),
    TOO_MANY_GLOBALS("too many global variables"),
    TOO_MANY_LOCALS("too many local variables"),

    // ----- error messages first used in SimpleCodeGenerationTest
    ARRAY_INDEX("array index must be an integer"),
    ARRAY_SIZE("array size must be an integer"),
    CANNOT_ASSIGN_TO("cannot store to operand kind {0}"),
    INCOMP_TYPES("incompatible types"),
    INVALID_CALL("invalid call of void method"),
    METH_NOT_FOUND("method {0} not found"),
    NO_ARRAY("indexed object is not an array"),
    NO_CLASS("dereferenced object is not a class"),
    NO_CLASS_TYPE("class type expected"),
    NO_INT_OPERAND("operand(s) must be of type int"),
    NO_OPERAND("cannot create code operand for this kind of symbol table object"),
    NO_VAL("value expected"),
    PRINT_VALUE("can only print int or char values"),
    READ_VALUE("can only read int or char values"),

    // ----- error messages first used in CodeGenerationTest
    EQ_CHECK("only (un)equality checks are allowed for reference types"),
    INVALID_METH_RETURN_TYPE("methods may only return int or char"),
    LESS_ACTUAL_PARAMS("less actual than formal parameters"),
    MORE_ACTUAL_PARAMS("more actual than formal parameters"),
    NO_LOOP("break is not within a loop"),
    NO_METH("called object is not a method"),
    PARAM_TYPE("parameter type mismatch"),
    RETURN_NO_VAL("return expression required"),
    NON_MATCHING_RETURN_TYPE("return type must match method type"),
    RETURN_VOID("void method must not return a value");

    private final String msg;

    Message(String msg) {
      this.msg = msg;
    }

    public String format(Object... params) {
      if (params.length != (msg.contains("{0}") ? 1 : 0)) {
        throw new Error("incorrect number of error message parameters");
      }
      return MessageFormat.format(msg, params);
    }
  }

  /**
   * List of error messages.
   */
  private final List<String> errors;

  /**
   * Initialization (must be called before compilation).
   */
  public Errors() {
    errors = new ArrayList<>();
  }

  /**
   * Add a new error message to the list of errors.
   */
  public void error(int line, int col, Message msg, Object... msgParams) {
    errors.add("-- line " + line + " col " + col + ": " + msg.format(msgParams));
  }

  /**
   * Returns the number of errors.
   */
  public int numErrors() {
    return errors.size();
  }

  /**
   * String representation for JUnit test cases.
   */
  public String dump() {
    StringBuilder sb = new StringBuilder();
    for (String error : errors) {
      sb.append(error).append("\n");
    }
    return sb.toString();
  }
}
