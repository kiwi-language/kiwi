package org.manul.object.type;

public class VoidSuperTypeVisitor extends SuperTypeVisitor<Void> {
    @Override
    public Void defaultValue() {
        return null;
    }
}
