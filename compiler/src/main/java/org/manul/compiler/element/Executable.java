package org.manul.compiler.element;

import org.manul.compiler.generate.Code;
import org.manul.compiler.type.Type;
import org.manul.compiler.util.List;

import javax.annotation.Nullable;


public interface Executable extends Element, VariableScope {

    List<Param> getParams();

    Type getRetType();

    void addParam(Param param);

    Name getQualName();

    @Nullable Code getCode();

    ConstPool getConstPool();
}
