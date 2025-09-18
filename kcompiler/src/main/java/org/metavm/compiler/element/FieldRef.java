package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ArrayType;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public interface FieldRef extends Variable, Constant, MemberRef {

    Name getName();

    boolean isStatic();

    ClassType getDeclType();

    Type getType();

    Element getElement();

    @Override
    default void write(MvOutput output) {
        output.write(WireTypes.FIELD_REF);
        getDeclType().write(output);
        Elements.writeReference(getElement(), output);
    }

    @Override
    default void load(Code code, Env env) {
        if (getElement() == ArrayType.lengthField)
            code.arrayLength();
        else if (isStatic())
           code.getStaticField(this);
        else
           code.getField(this);
    }

    @Override
    default void store(Code code, Env env) {
        if (getElement() == ArrayType.lengthField)
            throw new UnsupportedOperationException();
        if (isStatic())
            code.setStatic(this);
        else
            code.setField(this);
    }

    @Override
    default void storeRefresh(Code code, Env env) {
        if (getElement() == ArrayType.lengthField)
            throw new UnsupportedOperationException();
        if (isStatic())
            code.setStatic(this);
        else
            code.setFieldRefresh(this);
    }


}
