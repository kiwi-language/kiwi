package org.metavm.compiler.element;

import org.metavm.compiler.util.List;

import java.util.Collection;

public class BuiltinClassScope implements ClassScope {

    public static final BuiltinClassScope instance = new BuiltinClassScope();

    private List<Clazz> classes = List.of();

    private BuiltinClassScope() {
    }

    @Override
    public Collection<Clazz> getClasses() {
        return classes;
    }

    @Override
    public void addClass(Clazz clazz) {
        classes = classes.prepend(clazz);
    }

    @Override
    public Name getQualName() {
        return NameTable.instance.builtin;
    }
}
