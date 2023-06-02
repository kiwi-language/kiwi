package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;

public record MaxValue(
        INode node
) implements Value {

    @Override
    public IRType get() {
//        return null;
        return node.getMax();
    }

}
