package tech.metavm.object.meta;

public class VoidSuperTypeVisitor extends SuperTypeVisitor<Void> {
    @Override
    public Void defaultValue() {
        return null;
    }
}
