package org.manul.compiler.element;

import org.manul.compiler.analyze.Env;
import org.manul.compiler.generate.Code;
import org.manul.compiler.type.ClassType;

public interface MethodRef extends FuncRef, MemberRef {

    ClassType getDeclType();

    Access getAccess();

    boolean isInit();

    boolean isStatic();

    void invoke(Code code, Env env);

    Method getRawMethod();

}
