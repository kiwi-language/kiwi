package org.metavm.object.instance.core;

public abstract class StructuralInstanceVisitor {

    public void visitDurableInstance(DurableInstance instance) {
        instance.forEachMember(c -> c.accept(this));
    }

    public void visitClassInstance(ClassInstance instance) {
        visitDurableInstance(instance);
    }

    public void visitArrayInstance(ArrayInstance instance) {
        visitDurableInstance(instance);
    }

}
