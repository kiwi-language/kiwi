package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.LambdaType;
import tech.metavm.transpile.ir.IRType;

public class LNode extends SuperAwareNode {

    protected LNode(LambdaType type) {
        super(type);
    }

    @Override
    public LambdaType getType() {
        return (LambdaType) super.getType();
    }

    @Override
    protected boolean onSuperNodeAdded(Node node) {
        return node.addLowerBound(getType());
    }

    @Override
    public IRType solve() {
        return null;
    }

    @Override
    public boolean addLowerBound(IRType type) {
        return false;
    }

    @Override
    public boolean addUpperBound(IRType type) {
        return false;
    }

    @Override
    protected boolean assign(IRType type) {
        return false;
    }

    @Override
    protected Node copy(Graph graphCopy) {
        return null;
    }
}
