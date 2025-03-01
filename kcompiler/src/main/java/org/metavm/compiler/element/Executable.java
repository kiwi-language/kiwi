package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;


public interface Executable extends Element {

    List<Parameter> getParameters();

    Type getReturnType();

    void addParameter(Parameter parameter);

    SymName getQualifiedName();

    @Nullable Code getCode();

    ConstantPool getConstantPool();
}
