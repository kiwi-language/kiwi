package org.metavm.compiler.element;

public interface Member extends Element {

    Name getName();

    void setName(Name name);

    default boolean isPublic() {
        return getAccess() == Access.PUBLIC;
    }

    Access getAccess();

    void setAccess(Access access);

    Clazz getDeclClass();

    boolean isStatic();
}
