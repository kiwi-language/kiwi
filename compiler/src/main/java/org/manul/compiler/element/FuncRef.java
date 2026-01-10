package org.manul.compiler.element;

import org.manul.compiler.type.FuncType;
import org.manul.compiler.type.Type;
import org.manul.compiler.util.List;
import org.manul.compiler.generate.KlassOutput;

public interface FuncRef extends ValueElement, Constant {

     void write(KlassOutput output);

     List<Type> getTypeArgs();

     List<Type> getParamTypes();

     Type getRetType();

     FuncType getType();

}
