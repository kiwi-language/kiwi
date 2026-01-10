package org.manul.compiler.element;

import org.manul.compiler.analyze.Env;
import org.manul.compiler.generate.Code;
import org.manul.compiler.type.Type;

public interface Variable extends ValueElement {

     Name getName();

    void setName(Name name);

    Type getType();

    void setType(Type type);

    void load(Code code, Env env);

    void store(Code code, Env env);

    VariableScope getScope();

    boolean isMutable();
}
