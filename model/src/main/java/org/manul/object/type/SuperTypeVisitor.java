package org.manul.object.type;

import org.manul.entity.Element;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.InstanceVisitor;

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

