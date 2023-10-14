package ssw.mj.symtab;

import ssw.mj.impl.Tab;

import java.util.Collections;
import java.util.Map;

public final class Struct {

  /**
   * Possible codes for structure kinds.
   */
  public enum Kind {
    None, Int, Char, Arr, Class
  }

  /**
   * Kind of the structure node.
   */
  public final Kind kind;
  /**
   * Only for Arr: Type of the array elements.
   */
  public final Struct elemType;

  /**
   * Only for Class: First element of the linked list of local variables.
   *
   * This is a Collections.emptyMap() (which is immutable) on purpose, do not change this line.
   * When you finished reading the fields of a class, use clazz.fields = curScope.locals() and close the scope afterwards
   */
  public Map<String, Obj> fields = Collections.emptyMap();

  private Struct(Kind kind, Struct elemType) {
    this.kind = kind;
    this.elemType = elemType;
  }

  public Struct(Kind kind) {
    this(kind, null);
  }

  /**
   * Creates a new array structure with a specified element type.
   */
  public Struct(Struct elemType) {
    this(Kind.Arr, elemType);
  }

  /**
   * Retrieves the field <code>name</code>.
   */
  public Obj findField(String name) {
    return fields.get(name);
  }

  /**
   * Only for Class: Number of fields.
   */
  public int nrFields() {
    return fields.size();
  }

  @Override
  public String toString() {
    if (this == Tab.nullType) {
      return "null";
    }
    switch (kind) {
      case Int:
      case Char:
      case None:
        return kind.toString();
      case Arr:
        return elemType + "[]";
      case Class:
        StringBuilder sb = new StringBuilder();
        sb.append("Class{");
        boolean first = true;
        for (Map.Entry<String, Obj> e : fields.entrySet()) {
          String fieldName = e.getKey();
          Obj field = e.getValue();
          if (!first) {
            sb.append(", ");
          }
          sb.append(fieldName).append('=').append(field.type);
          first = false;
        }
        sb.append('}');
        return sb.toString();
    }
    throw new RuntimeException("Unknown Struct " + kind);
  }

  public boolean isRefType() {
    return kind == Kind.Class || kind == Kind.Arr;
  }

  public boolean isEqual(Struct other) {
    if (kind == Kind.Arr) {
      return other.kind == Kind.Arr && elemType.isEqual(other.elemType);
    }
    return this == other;
  }

  public boolean compatibleWith(Struct other) {
    return this.isEqual(other)
            || (this == Tab.nullType && other.isRefType())
            || (other == Tab.nullType && this.isRefType());
  }

  public boolean assignableTo(Struct dest) {
    return this.isEqual(dest) || (this == Tab.nullType && dest.isRefType())
            // this is necessary for the standard function len
            || (this.kind == Kind.Arr && dest.kind == Kind.Arr
            && dest.elemType == Tab.noType);
  }
}
