package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.IRType;

public class RangeNodeV3 extends AbsRangeNode {

    RangeNodeV3(TypeRange type, TypeGraph graph) {
        super(type, graph, graph.getNode(type.getLowerBound()), graph.getNode(type.getUpperBound()));
    }

    @Override
    public TypeRange getType() {
        return (TypeRange) super.getType();
    }

//    @Override
//    public IRType getOuterRange() {
//        return TypeRange.between(lowerBound.getLowerBoundMin(), upperBound.getUpperBoundMax());
//    }
//
//    @Override
//    public IRType getInnerRange() {
//        return TypeRange.between(lowerBound.getUpperBoundMax(), upperBound.getLowerBoundMin());
//    }

    @Override
    public boolean assign(INode node) {
        return ge(node.getUpperBound()) && le(node.getLowerBound());
    }

    @Override
    public IRType get() {
        return TypeRange.between(lowerBound.get(), upperBound.get());
    }

}
