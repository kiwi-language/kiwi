package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;

public record MinValue(
        INode node
) implements Value {

    @Override
    public IRType get() {
        return node.getMin();
//        return null;
    }
}
