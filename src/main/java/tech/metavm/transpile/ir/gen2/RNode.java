package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.IRWildCardType;

public class RNode extends Node {

    private final Node lowerBound;
    private final Node upperBound;

    protected RNode(IRWildCardType type, Graph graph) {
        super(type);
        lowerBound = graph.getNode(type.getLowerBound());
        upperBound = graph.getNode(type.getUpperBound());
    }

    public RNode(IRType type, Node lowerBound, Node upperBound) {
        super(type);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    @Override
    public IRType solve() {
        return TypeRange.between(lowerBound.solve(), upperBound.solve());
    }

    @Override
    public boolean addLowerBound(IRType type) {
        return lowerBound.addLowerBound(type);
    }

    @Override
    public boolean addUpperBound(IRType type) {
        return upperBound.addUpperBound(type);
    }

    @Override
    protected boolean assign(IRType type) {
        return lowerBound.addUpperBound(type) && upperBound.addLowerBound(type);
    }

    @Override
    protected RNode copy(Graph graphCopy) {
        return new RNode(getType(), graphCopy.copyNode(lowerBound), graphCopy.copyNode(upperBound));
    }
}
