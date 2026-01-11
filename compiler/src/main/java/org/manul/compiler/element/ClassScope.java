package org.manul.compiler.element;

import java.util.Collection;

public interface ClassScope {

    Collection<Clazz> getClasses();

    void addClass(Clazz clazz);

    Name getQualName();
}
