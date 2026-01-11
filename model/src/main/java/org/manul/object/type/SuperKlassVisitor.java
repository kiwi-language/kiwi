package org.manul.object.type;

import org.manul.entity.Element;
import org.manul.entity.ElementVisitor;

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
