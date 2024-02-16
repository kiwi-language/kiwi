package tech.metavm.object.instance.core;

import tech.metavm.entity.natives.ListNative;

public class CollectionAwareStructuralVisitor extends StructuralVisitor {

    @Override
    public Void visitClassInstance(ClassInstance instance) {
        if(instance.getType().isListType())
            return visitListInstance(instance);
        else
            return super.visitClassInstance(instance);
    }

    public Void visitListInstance(ClassInstance instance) {
        if(instance.getType().isChildListType())
            new ListNative(instance).toArray().getElements().forEach(element -> element.accept(this));
        return null;
    }

}
