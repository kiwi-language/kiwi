package org.metavm.compiler.element;

import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

public interface FuncRef extends ValueElement, Constant {

     void write(MvOutput output);

     List<Type> getTypeArgs();

     List<Type> getParamTypes();

     Type getRetType();

     FuncType getType();

}
