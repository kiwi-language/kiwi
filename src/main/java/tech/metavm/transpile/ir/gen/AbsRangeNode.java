package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.IRType;

public abstract class AbsRangeNode extends NodeBase {

    protected final INode lowerBound;
    protected final INode upperBound;

    protected AbsRangeNode(IRType type, TypeGraph graph, INode lowerBound, INode upperBound) {
        super(type, graph);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        lowerBound.addSuperNode(upperBound);
    }

//    @Override
//    public IRType getMax() {
//        return TypeRange.between(lowerBound.getMax(), upperBound.getMax());
//    }

//    @Override
//    public IRType getMin() {
//        return TypeRange.between(lowerBound.getMin(), upperBound.getMin());
//    }

    @Override
    public INode getLowerBound() {
        return lowerBound;
    }

    @Override
    public INode getUpperBound() {
        return upperBound;
    }

    @Override
    public IRType solve() {
        return TypeRange.between(lowerBound.solve(), upperBound.solve());
    }

    @Override
    public boolean ge(INode value) {
        return lowerBound.ge(value);
    }

    @Override
    public boolean le(INode value) {
        return upperBound.le(value);
    }
}
