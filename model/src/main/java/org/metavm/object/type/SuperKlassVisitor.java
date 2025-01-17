package org.metavm.object.type;

import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;

public class SuperKlassVisitor<R> extends ElementVisitor<R> {

    @Override
    public R visitElement(Element element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public R visitKlass(Klass klass) {
        klass.forEachSuper(k -> k.accept(this));
        return super.visitKlass(klass);
    }
}
