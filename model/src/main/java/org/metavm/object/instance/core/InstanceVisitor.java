package org.metavm.object.instance.core;

public abstract class InstanceVisitor {

    public void visitInstance(Instance instance) {}

    public void visitClassInstance(ClassInstance instance) {
        visitInstance(instance);
    }

    public void visitArrayInstance(ArrayInstance instance) {
        visitInstance(instance);
    }

}
