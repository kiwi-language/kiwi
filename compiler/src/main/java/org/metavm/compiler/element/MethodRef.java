package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;

public interface MethodRef extends FuncRef, MemberRef {

    ClassType getDeclType();

    Access getAccess();

    boolean isInit();

    boolean isStatic();

    void invoke(Code code, Env env);

    Method getRawMethod();

}
