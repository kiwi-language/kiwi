package org.metavm.object.instance.core;

import org.metavm.entity.natives.ListNative;

public abstract class CollectionAwareStructuralVisitor extends StructuralInstanceVisitor {

    @Override
    public void visitInstance(Instance instance) {
        if(instance instanceof ClassInstance classInstance && classInstance.isList()) {
            if(classInstance.isChildList())
                for (Value element : new ListNative(classInstance).toArray().getElements()) {
                    if(element instanceof Reference r)
                        r.resolve().accept(this);
                }
        }
        else
            super.visitInstance(instance);
    }
}
