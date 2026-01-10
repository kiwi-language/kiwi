package org.manul.compiler.element;

import org.manul.compiler.analyze.Env;
import org.manul.compiler.generate.Code;
import org.manul.compiler.type.ClassType;
import org.manul.compiler.type.Type;

public interface MemberRef {

    ClassType getDeclType();

    Name getName();

    void load(Code code, Env env);

    void store(Code code, Env env);

    default void storeRefresh(Code code, Env env) {
        store(code, env);
    }

    void invoke(Code code, Env env);

    Type getType();

    boolean isStatic();
}
