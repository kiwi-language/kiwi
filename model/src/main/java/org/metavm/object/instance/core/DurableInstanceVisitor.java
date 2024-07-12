package org.metavm.object.instance.core;

public abstract class DurableInstanceVisitor {

    public void visitDurableInstance(DurableInstance instance) {}

    public void visitClassInstance(ClassInstance instance) {
        visitDurableInstance(instance);
    }

    public void visitArrayInstance(ArrayInstance instance) {
        visitDurableInstance(instance);
    }

}
