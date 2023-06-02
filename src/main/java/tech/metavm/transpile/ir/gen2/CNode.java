package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.IRType;

public class CNode extends Node {

    public CNode(IRType klass) {
        super(klass);
    }

    @Override
    public boolean addLowerBound(IRType type) {
        return getType().isAssignableFrom(type);
    }

    @Override
    public boolean addUpperBound(IRType type) {
        return type.isTypeAssignableFrom(getType());
    }

    @Override
    protected boolean assign(IRType type) {
        if(getType() instanceof TypeRange range) {
            return range.contains(type);
        }
        else {
            return getType().typeEquals(type);
        }
    }

    @Override
    protected Node copy(Graph graphCopy) {
        return this;
    }

    @Override
    public IRType solve() {
        return getType();
    }

}
