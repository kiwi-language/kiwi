package tech.metavm.object.instance.core;

public abstract class StructuralVisitor extends VoidInstanceVisitor {

//    private final InstanceVisitor noRepeatVisitor = new InstanceVisitor() {
//        @Override
//        public void visitInstance(Instance instance) {
//            numCalls++;
//            instance.accept(StructuralVisitor.this);
//        }
//    };

    @Override
    public Void visitInstance(Instance instance) {
        instance.acceptChildren(this);
        return super.visitInstance(instance);
    }

}
