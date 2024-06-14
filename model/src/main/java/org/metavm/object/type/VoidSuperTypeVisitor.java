package org.metavm.object.type;

public class VoidSuperTypeVisitor extends SuperTypeVisitor<Void> {
    @Override
    public Void defaultValue() {
        return null;
    }
}
