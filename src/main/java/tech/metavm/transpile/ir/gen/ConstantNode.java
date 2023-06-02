package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;

public class ConstantNode extends NodeBase {

    protected ConstantNode(IRType type, TypeGraph graph) {
        super(type, graph);
    }

    @Override
    public IRType solve() {
        return getType();
    }

    @Override
    public boolean ge(INode value) {
        return getType().isAssignableFrom(value.get());
    }

    @Override
    public boolean le(INode value) {
        return value.get().isAssignableFrom(getType());
    }

    @Override
    public boolean assign(INode node) {
        return getType().isTypeAssignableFrom(node.get());
    }

    public boolean eq(Value value) {
        return getMax().isAssignableFrom(value.get()) && value.get().isAssignableFrom(getMin());
    }

    @Override
    public IRType getMax() {
        return getType().getUpperBound();
    }

    @Override
    public IRType getMin() {
        return getType().getLowerBound();
    }

    @Override
    public INode getLowerBound() {
        return graph.getNode(getType().getLowerBound());
    }

    @Override
    public INode getUpperBound() {
        return graph.getNode(getType().getUpperBound());
    }

    @Override
    public IRType get() {
        return getType();
    }

}
