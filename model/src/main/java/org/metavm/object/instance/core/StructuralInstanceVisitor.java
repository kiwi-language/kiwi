package org.metavm.object.instance.core;

public abstract class StructuralInstanceVisitor extends DurableInstanceVisitor {

    @Override
    public void visitDurableInstance(DurableInstance instance) {
        instance.forEachMember(c -> c.accept(this));
    }

}