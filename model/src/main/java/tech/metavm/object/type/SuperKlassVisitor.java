package tech.metavm.object.type;

import tech.metavm.entity.ElementVisitor;

public class SuperKlassVisitor<R> extends ElementVisitor<R> {

    @Override
    public R visitKlass(Klass klass) {
        klass.forEachSuper(k -> k.accept(this));
        return super.visitKlass(klass);
    }
}
