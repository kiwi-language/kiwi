package org.manul.object.type;

import org.manul.entity.Element;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.InstanceVisitor;

public abstract class SubTypesVisitor<R> extends ElementVisitor<R> {
    @Override
    public R visitElement(Element element) {
        return defaultValue();
    }

    public abstract R defaultValue();

    @Override
    public R visitKlass(Klass klass) {
        for (Klass subType : klass.getSubKlasses()) {
            subType.accept(this);
        }
        return super.visitKlass(klass);
    }

}
