package org.metavm.compiler.element;

import java.util.Collection;

public interface ClassScope {

    Collection<Clazz> getClasses();

    void addClass(Clazz clazz);

    SymName getQualifiedName();
}
