package org.manul.compiler.element;

import org.manul.compiler.analyze.Env;
import org.manul.compiler.generate.Code;
import org.manul.compiler.type.ArrayType;
import org.manul.compiler.type.ClassType;
import org.manul.compiler.type.Type;
import org.manul.compiler.generate.KlassOutput;
import org.manul.compiler.generate.WireTypes;

public interface FieldRef extends Variable, Constant, MemberRef {

    Name getName();

    boolean isStatic();

    ClassType getDeclType();

    Type getType();

    Element getElement();

    @Override
    default void write(KlassOutput output) {
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
