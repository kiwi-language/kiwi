//package tech.metavm.transpile.ir.gen;
//
//import tech.metavm.transpile.TypeRange;
//import tech.metavm.transpile.ir.IRType;
//
//public class RangeNodeV2 extends AtomicNode {
//
//    private final INode lowerBound;
//    private final INode upperBound;
//
//    RangeNodeV2(TypeRange type, INode lowerBound, INode upperBound) {
//        super(type);
//        this.lowerBound = lowerBound;
//        this.upperBound = upperBound;
//        lowerBound.addSuperNode(upperBound);
//    }
//
//    @Override
//    public TypeRange getType() {
//        return (TypeRange) super.getType();
//    }
//
//    @Override
//    protected boolean setRange0(IRType range) {
//        return lowerBound.setLowerBoundMin(range.getLowerBound())
//                && upperBound.setUpperBoundMax(range.getUpperBound());
//    }
//
//    @Override
//    protected boolean setInnerRange0(IRType range) {
//        return lowerBound.setUpperBoundMax(range.getLowerBound())
//                && upperBound.setLowerBoundMin(range.getUpperBound());
//    }
//
//    @Override
//    public IRType getOuterRange() {
//        return TypeRange.between(lowerBound.getLowerBoundMin(), upperBound.getUpperBoundMax());
//    }
//
//    @Override
//    public IRType getInnerRange() {
//        return TypeRange.between(lowerBound.getUpperBoundMax(), upperBound.getLowerBoundMin());
//    }
//}
