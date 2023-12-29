package tech.metavm.object.instance.core;

public class VoidInstanceVisitor extends InstanceVisitor<Void> {

    @Override
    public Void visitInstance(Instance instance) {
        return null;
    }
}
