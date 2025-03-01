package org.metavm.compiler.element;

import org.metavm.compiler.syntax.Expr;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public interface FuncInst extends ValueElement, Constant {
    Func getFunction();

    FuncInst getInstance(List<Type> typeArguments);

    List<Type> getParameterTypes();

    Type getReturnType();

    List<Type> getTypeArguments();

}
