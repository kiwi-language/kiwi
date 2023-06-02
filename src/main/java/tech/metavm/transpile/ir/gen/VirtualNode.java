package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;

public interface VirtualNode extends INode {

    @Override
    default IRType getType() {
        return null;
    }

    @Override
    default boolean ge(INode node) {
        return true;
    }

    @Override
    default boolean le(INode node) {
        return true;
    }

    @Override
    default boolean assign(INode type) {
        return true;
    }

}
