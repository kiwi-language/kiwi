package org.metavm.compiler.type;

import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.TypeNode;

import javax.annotation.Nullable;

public interface Type extends Constant {

    void writeType(ElementWriter writer);

    default String getTypeText() {
        var w = new ElementWriter();
        w.writeType(this);
        return w.toString();
    }

    boolean isAssignableFrom(Type type);

    <R> R accept(TypeVisitor<R> visitor);

    default boolean contains(Type that) {
        return this == that;
    }

    default Type getUpperBound() {
        return this;
    }

    int getTag();

    ElementTable getTable();


    String getInternalName(@Nullable Func current);

    default Type toStackType() {
        return this;
    }

    default Type getUnderlyingType() {
        return this;
    }

    default boolean isPrimitive() {
        return false;
    }

    default boolean isVoid() {
        return false;
    }

    TypeNode makeNode();

    Closure getClosure();

    default boolean isNullable() {
        return false;
    }

}
