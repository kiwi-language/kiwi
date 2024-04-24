package tech.metavm.object.type;

import tech.metavm.entity.Element;
import tech.metavm.entity.ElementVisitor;

public abstract class SubTypesVisitor<R> extends ElementVisitor<R> {
    @Override
    public R visitElement(Element element) {
        return defaultValue();
    }

    public abstract R defaultValue();

    @Override
    public R visitKlass(Klass klass) {
        for (Klass subType : klass.getSubTypes()) {
            subType.accept(this);
        }
        return super.visitKlass(klass);
    }

}
