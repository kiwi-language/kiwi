package org.metavm.object.instance.core;

import org.metavm.object.type.Field;

public abstract class StructuralVisitor extends VoidInstanceVisitor {

    private DurableInstance parent;

    private Field parentField;

    private int index = -1;

//    private final InstanceVisitor noRepeatVisitor = new InstanceVisitor() {
//        @Override
//        public void visitInstance(Instance instance) {
//            numCalls++;
//            instance.accept(StructuralVisitor.this);
//        }
//    };

    @Override
    public Void visitClassInstance(ClassInstance instance) {
        var oldParent = parent;
        var oldParentField = parentField;
        var oldIndex = index;
        parent = instance;
        instance.forEachField((field, value) -> {
            if(field.isChild() && value.isNotNull()) {
                parentField = field;
                value.accept(this);
            }
        });
        parent = oldParent;
        parentField = oldParentField;
        index = oldIndex;
        return super.visitClassInstance(instance);
    }

    @Override
    public Void visitArrayInstance(ArrayInstance instance) {
        if(instance.isChildArray()) {
            var oldParent = parent;
            var oldParentField = parentField;
            var oldIndex = index;
            parent = instance;
            index = 0;
            for (Instance element : instance) {
                element.accept(this);
                index++;
            }
            parent = oldParent;
            parentField = oldParentField;
            index = oldIndex;
        }
        return super.visitArrayInstance(instance);
    }

    public DurableInstance getParent() {
        return parent;
    }

    public Field getParentField() {
        return parentField;
    }

    public int getIndex() {
        return index;
    }
}
