//package tech.metavm.transpile.ir.gen;
//
//import tech.metavm.transpile.TypeRange;
//import tech.metavm.transpile.ir.IRType;
//import tech.metavm.transpile.ir.TypeUnion;
//import tech.metavm.util.NncUtils;
//
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.Queue;
//import java.util.Set;
//
//public class UnionNodeV2 extends AtomicNode {
//
//    private IRType range;
//    private IRType innerRange;
//    private final Set<INode> members;
//
//    protected UnionNodeV2(TypeUnion type, Set<INode> members) {
//        super(type);
//        this.members = new HashSet<>(members);
//        range = rebuildRange();
//        innerRange = rebuildInnerRange();
//        for (INode member : members) {
//            member.addListener(this::onMemberChange);
//        }
//    }
//
//    @Override
//    public TypeUnion getType() {
//        return (TypeUnion) super.getType();
//    }
//
//    @Override
//    protected boolean setRange0(IRType range) {
//        Queue<INode> lowerMatchingNodes = new LinkedList<>();
//        for (INode member : members) {
//            if(!member.setUpperBoundMax(range.getUpperBound())) {
//                return false;
//            }
//            if(member.getUpperBoundMax().isAssignableFrom(range.getLowerBound())) {
//                lowerMatchingNodes.offer(member);
//            }
//        }
//        if(lowerMatchingNodes.isEmpty()) {
//            return false;
//        }
//        else if(lowerMatchingNodes.size() == 1) {
//            lowerMatchingNodes.peek().setLowerBoundMin(range.getLowerBound());
//        }
//        this.range = range;
//        return true;
//    }
//
//    @Override
//    protected boolean setInnerRange0(IRType range) {
//        Queue<INode> upperBoundMatches = new LinkedList<>();
//        for (INode member : members) {
//            if(!member.setLowerBoundMax(range.getLowerBound())) {
//                return false;
//            }
//            if(member.getUpperBoundMax().isAssignableFrom(range.getUpperBound())) {
//                upperBoundMatches.offer(member);
//            }
//        }
//        if(upperBoundMatches.isEmpty()) {
//            return false;
//        }
//        if(upperBoundMatches.size() == 1) {
//            upperBoundMatches.peek().setUpperBoundMin(range.getUpperBound());
//        }
//        this.innerRange = range;
//        return true;
//    }
//
//    private boolean onMemberChange() {
//        return setOuterRange(rebuildRange()) && setInnerRange(rebuildInnerRange());
//    }
//
//    private IRType rebuildRange() {
//        return TypeRange.between(
//                TypeUnion.of(NncUtils.map(members, INode::getLowerBoundMin)),
//                TypeUnion.of(NncUtils.map(members, INode::getUpperBoundMax))
//        );
//    }
//
//    private IRType rebuildInnerRange() {
//        return TypeRange.between(
//                TypeUnion.of(NncUtils.map(members, INode::getLowerBoundMax)),
//                TypeUnion.of(NncUtils.map(members, INode::getUpperBoundMin))
//        );
//    }
//
//    @Override
//    public IRType getOuterRange() {
//        return range;
//    }
//
//    @Override
//    public IRType getInnerRange() {
//        return innerRange;
//    }
//}
