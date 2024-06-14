package org.metavm.object.instance.core;

import org.metavm.entity.natives.ListNative;

public class CollectionAwareStructuralVisitor extends StructuralVisitor {

    @Override
    public Void visitClassInstance(ClassInstance instance) {
        if(instance.getType().isList())
            return visitListInstance(instance);
        else
            return super.visitClassInstance(instance);
    }

    public Void visitListInstance(ClassInstance instance) {
        if(instance.getType().isChildList())
            new ListNative(instance).toArray().getElements().forEach(element -> element.accept(this));
        return null;
    }

}
