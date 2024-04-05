package tech.metavm.object.instance.core;

import tech.metavm.util.IdentitySet;

public abstract class GraphVisitor extends VoidInstanceVisitor {

    protected final IdentitySet<Instance> visited = new IdentitySet<>();
    public int numCalls;

    private final VoidInstanceVisitor noRepeatVisitor = new VoidInstanceVisitor() {
        @Override
        public Void visitInstance(Instance instance) {
            numCalls++;
            if(!visited.contains(instance))
                instance.accept(GraphVisitor.this);
            return null;
        }
    };

    public final Void visit(Instance instance) {
        instance.accept(noRepeatVisitor);
        return null;
    }

    @Override
    public Void visitInstance(Instance instance) {
        numCalls++;
        if(!visited.add(instance))
            return null;
        instance.acceptReferences(noRepeatVisitor);
        return super.visitInstance(instance);
    }

}
