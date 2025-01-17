package org.metavm.object.type;

import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceVisitor;

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
