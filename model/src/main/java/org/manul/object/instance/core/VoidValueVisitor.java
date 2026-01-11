package org.manul.object.instance.core;

public class VoidValueVisitor extends ValueVisitor<Void> {

    @Override
    public Void visitValue(Value instance) {
        return null;
    }
}
