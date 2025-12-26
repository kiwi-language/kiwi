package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

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
