package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public interface FieldRef extends Variable, Constant, MemberRef {

    SymName getName();

    boolean isStatic();

    ClassType getDeclaringType();

    Type getType();

    Element getElement();

    @Override
    default void write(MvOutput output) {
        output.write(WireTypes.FIELD_REF);
        getDeclaringType().write(output);
        Elements.writeReference(getElement(), output);
    }

    @Override
    default void load(Code code) {
       if (isStatic())
           code.getStaticField(this);
       else
           code.getField(this);
    }

    @Override
    default void store(Code code) {
        if (isStatic())
            code.setStatic(this);
        else
            code.setField(this);
    }
}
