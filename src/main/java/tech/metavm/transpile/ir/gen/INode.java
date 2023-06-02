package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;

public interface INode extends Value {

    IRType getType();

    default void addListener(NodeListener listener) {}

//    default boolean setOuterRange(INode range) {
//        return getLowerBound().addExtendingNode(range.getLowerBound())
//                && getUpperBound().addSuperNode(range.getUpperBound());
//    }
//
//    default IRType getOuterRange() {
//        return TypeRange.between(getLowerBoundMin(), getUpperBoundMax());
//    }
//
//    default IRType getInnerRange() {
//        if(getUpperBoundMax() == null) {
//            return null;
//        }
//        return TypeRange.between(getLowerBoundMax(), getUpperBoundMin());
//    }

    default IRType getMax() {
        return get();
    }

    default IRType getMin() {
        return get();
    }

    default INode getLowerBound() {
        return this;
    }

    default INode getUpperBound() {
        return this;
    }

    default IRType getMinRange() {
        return get();
//        if(getLowerBound().equals(getUpperBound())) {
//            return get();
//        }
//        else {
//            return TypeRange.between(getLowerBoundMin(), getLowerBoundMin());
//        }
    }

    default IRType getMaxRange() {
        return get();
//        if(getLowerBound().equals(getUpperBound())) {
//            return get();
//        }
//        else {
//            return TypeRange.between(getLowerBound().get(), getUpperBound().get());
//        }
    }

    IRType get();

//    default INode getUltimateUpperBound() {
//        var u = getUpperBound();
//        while (u.getUpperBound() != u) {
//            u = u.getUpperBound();
//        }
//        return u;
//    }
//
//    default INode getUltimateLowerBound() {
//        var l = getLowerBound();
//        while (l.getLowerBound() != l) {
//            l = l.getLowerBound();
//        }
//        return l;
//    }

//    boolean addLowerBound(INode lowerBound);

//    boolean addUpperBound(INode upperBound);

//    default IRType getLowerBoundMin() {
//        return getLowerBound().getMin();
//    }

//    default IRType getLowerBoundMax() {
//        return getLowerBound().getMax();
//    }

//    default IRType getUpperBoundMin() {
//        return getUpperBound().getMin();
//    }

//    default IRType getUpperBoundMax() {
//        return getUpperBound().getMax();
//    }

//    default boolean setInnerRange(INode range) {
//        return getLowerBound().addSuperNode(range.getLowerBound())
//                && getUpperBound().addExtendingNode(range.getUpperBound());
//    }

//    default boolean setLowerBoundMin(IRType lowerBound) {
//        return getLowerBound().addLowerBound(lowerBound);
//    }
//
//    default boolean setLowerBoundMax(IRType lowerBoundMax) {
//        return getLowerBound().addUpperBound(lowerBoundMax);
//    }

//    default boolean setLowerBoundMax(IRType lowerBoundMin) {
//        return setInnerRange(TypeRange.between(lowerBoundMin, getUpperBoundMin()));

//    }

//    default boolean setUpperBoundMin(IRType upperBoundMin) {
//        return getUpperBound().addLowerBound(upperBoundMin);
//    }
//
//    default boolean setUpperBoundMax(IRType upperBound) {
//        return getUpperBound().addUpperBound(upperBound);
//    }

//    default boolean setUpperBoundMin(IRType upperBoundMax) {

//        return setInnerRange(TypeRange.between(getLowerBoundMax(), upperBoundMax));

//    }

    IRType solve();

    default boolean addSuperNode(INode node) {
        return le(node) && node.ge(this);
    }

    default boolean addExtendingNode(INode node) {
        return node.addSuperNode(this);
    }

    boolean ge(INode node);

    boolean le(INode node);

//    boolean eq(Range range);

    boolean assign(INode node);

    default boolean canCapture(INode node) {
        return get().isTypeAssignableFrom(node.get());
    }

//    boolean addEqualNode(INode node);
//    default boolean setLowerBoundMin(IRType lowerBound) {
//        return setRange(TypeRange.between(lowerBound, getUpperBoundMax()));

//    }

//    default boolean setUpperBoundMax(IRType upperBound) {
//        return setRange(TypeRange.between(getLowerBoundMin(), upperBound));
//    }

}
