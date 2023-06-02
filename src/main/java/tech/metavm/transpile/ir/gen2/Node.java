package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ir.IRType;

import java.util.function.Function;

public abstract class Node {

    private final IRType type;

    protected Node(IRType type) {
        this.type = type;
    }


    boolean within(IRType type) {
        return addUpperBound(type.getUpperBound()) && addLowerBound(type.getLowerBound());
    }

    public abstract IRType solve();

    public IRType getType() {
        return type;
    }

    public final boolean isConstant() {
        return type.isConstant();
    }

    public abstract boolean addLowerBound(IRType type);

    public abstract boolean addUpperBound(IRType type);

    protected abstract boolean assign(IRType type);

    protected abstract Node copy(Graph graphCopy);
}
