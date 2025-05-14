package org.metavm.compiler.element;

import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public interface FuncInst extends ValueElement, Constant {
    FuncRef getFunc();

    List<Type> getParamTypes();

    Type getRetType();

    List<Type> getTypeArgs();

}
