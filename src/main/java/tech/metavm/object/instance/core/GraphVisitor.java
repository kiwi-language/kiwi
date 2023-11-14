package tech.metavm.object.instance.core;

import tech.metavm.util.IdentitySet;

public abstract class GraphVisitor extends InstanceVisitor {

    private final IdentitySet<Instance> visited = new IdentitySet<>();
    public int numCalls;

    private final InstanceVisitor noRepeatVisitor = new InstanceVisitor() {
        @Override
        public void visitInstance(Instance instance) {
            numCalls++;
            if(!visited.contains(instance))
                instance.accept(GraphVisitor.this);
        }
    };

    public final void visit(Instance instance) {
        instance.accept(noRepeatVisitor);
    }

    @Override
    public void visitInstance(Instance instance) {
        numCalls++;
        if(!visited.add(instance))
            return;
        instance.acceptReferences(noRepeatVisitor);
        super.visitInstance(instance);
    }

}
