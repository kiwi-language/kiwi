package tech.metavm.object.meta;

import tech.metavm.entity.Element;
import tech.metavm.entity.ElementVisitor;

public abstract class SuperTypeVisitor<R> extends ElementVisitor<R> {

    public abstract R defaultValue();

    @Override
    public final R visitElement(Element element) {
        return defaultValue();
    }

    @Override
    public R visitType(Type type) {
        for (Type superType : type.getSuperTypes()) {
            superType.accept(this);
        }
        return super.visitType(type);
    }
}

