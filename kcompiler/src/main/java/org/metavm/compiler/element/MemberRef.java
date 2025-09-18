package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;

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
