package org.metavm.object.instance.core;

public abstract class StructuralInstanceVisitor extends InstanceVisitor {

    @Override
    public void visitInstance(Instance instance) {
        instance.forEachMember(c -> c.accept(this));
    }

}