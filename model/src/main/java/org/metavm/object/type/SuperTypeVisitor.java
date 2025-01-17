package org.metavm.object.type;

import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;

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

