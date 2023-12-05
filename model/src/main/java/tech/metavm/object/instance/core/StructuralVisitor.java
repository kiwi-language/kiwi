package tech.metavm.object.instance.core;

public abstract class StructuralVisitor extends InstanceVisitor {

    public int numCalls;

//    private final InstanceVisitor noRepeatVisitor = new InstanceVisitor() {
//        @Override
//        public void visitInstance(Instance instance) {
//            numCalls++;
//            instance.accept(StructuralVisitor.this);
//        }
//    };

    public final void visit(Instance instance) {
        instance.accept(this);
    }

    @Override
    public void visitInstance(Instance instance) {
        numCalls++;
        instance.acceptChildren(this);
        super.visitInstance(instance);
    }

}
