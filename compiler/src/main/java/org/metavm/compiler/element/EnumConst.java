package org.metavm.compiler.element;

import org.metavm.compiler.type.Type;

import static org.metavm.entity.FieldFlags.FLAG_ENUM_CONSTANT;
import static org.metavm.entity.FieldFlags.FLAG_STATIC;


public final class EnumConst extends Field implements FieldRef {

    private int ordinal;

    public EnumConst(Name name, int ordinal, Clazz declaringClass, Type type) {
        super(name, declaringClass, Access.PUBLIC, true, false, false, declaringClass);
        this.ordinal = ordinal;
        declaringClass.addEnumConstant(this);
    }

    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitEnumConstant(this);
    }

    public void write(ElementWriter writer) {
        writer.write(getName());
    }

    public Integer getSourceTag() {
        return null;
    }

    public int getFlags() {
        return FLAG_STATIC | FLAG_ENUM_CONSTANT;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }
}
