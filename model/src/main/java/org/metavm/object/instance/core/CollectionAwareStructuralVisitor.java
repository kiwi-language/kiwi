package org.metavm.object.instance.core;

import org.metavm.util.Instances;

public abstract class CollectionAwareStructuralVisitor extends StructuralInstanceVisitor {

    @Override
    public Void visitInstance(Instance instance) {
        if(instance instanceof MvClassInstance classInstance && classInstance.isList()) {
            if(classInstance.isChildList())
                for (Value element :  Instances.toJavaList(classInstance)) {
                    if(element instanceof Reference r)
                        r.get().accept(this);
                }
        }
        else
            super.visitInstance(instance);
        return null;
    }
}
