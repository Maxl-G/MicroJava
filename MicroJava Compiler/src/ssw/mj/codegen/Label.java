package ssw.mj.codegen;

import ssw.mj.impl.Code;

import java.util.ArrayList;
import java.util.List;

public final class Label {

  /**
   * Jump destination address.
   */
  private int adr;

  /**
   * List of unresolved forward jumps
   */
  private List<Integer> fixupList;

  /**
   * The code buffer this Label belongs to.
   */
  private final Code code;

  public Label(Code code) {
    this.code = code;
    fixupList = new ArrayList<>();
  }

  /**
   * Generates code for a jump to this label.
   */
  public void put() {
    if (isDefined()) {
      // jump destination already known
      code.put2(adr - (code.pc - 1));
    } else {
      // remember address to patch
      fixupList.add(code.pc);
      // insert place holder
      code.put2(0);
    }
  }

  /**
   * Defines <code>this</code> label to be at the current pc position
   */
  public void here() {
    if (isDefined()) {
      // should never happen
      throw new IllegalStateException("label defined twice");
    }

    for (int pos : fixupList) {
      code.put2(pos, code.pc - (pos - 1));
    }

    fixupList = null;
    adr = code.pc;
  }

  private boolean isDefined() {
    return fixupList == null;
  }
}
