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
    public R visitClassType(ClassType type) {
        for (ClassType subType : type.getSubTypes()) {
            subType.accept(this);
        }
        return super.visitClassType(type);
    }

}
