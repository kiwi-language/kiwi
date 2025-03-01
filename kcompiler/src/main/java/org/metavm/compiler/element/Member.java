package org.metavm.compiler.element;

public interface Member extends Element {

    SymName getName();

    void setName(SymName name);

    Access getAccess();

    void setAccess(Access access);

    Clazz getDeclaringClass();

}
