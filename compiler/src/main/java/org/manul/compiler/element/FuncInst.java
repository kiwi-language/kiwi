package org.manul.compiler.element;

import org.manul.compiler.type.Type;
import org.manul.compiler.util.List;

public interface FuncInst extends ValueElement, Constant {
    FuncRef getFunc();

    List<Type> getParamTypes();

    Type getRetType();

    List<Type> getTypeArgs();

}
