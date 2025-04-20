package org.metavm.compiler.element;

public interface Member extends Element {

    Name getName();

    void setName(Name name);

    Access getAccess();

    void setAccess(Access access);

    Clazz getDeclClass();

}
