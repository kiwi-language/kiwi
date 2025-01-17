package org.metavm.object.instance.core;

public abstract class StructuralInstanceVisitor extends InstanceVisitor<Void> {

    @Override
    public Void visitInstance(Instance instance) {
        instance.forEachMember(c -> c.accept(this));
        return null;
    }

}