package org.metavm.object.instance.core;

import org.metavm.entity.natives.ListNative;

public abstract class CollectionAwareStructuralVisitor extends StructuralInstanceVisitor {

    @Override
    public void visitDurableInstance(DurableInstance instance) {
        if(instance instanceof ClassInstance classInstance && classInstance.isList()) {
            if(classInstance.isChildList())
                for (Instance element : new ListNative(classInstance).toArray().getElements()) {
                    if(element instanceof InstanceReference r)
                        r.resolve().accept(this);
                }
        }
        else
            super.visitDurableInstance(instance);
    }
}
