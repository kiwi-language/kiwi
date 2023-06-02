//package tech.metavm.transpile.ir.gen;
//
//import tech.metavm.transpile.ir.*;
//import tech.metavm.util.InternalException;
//import tech.metavm.util.NncUtils;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.List;
//
//public class PNodeV2 extends AtomicNode {
//
//    @Nullable
//    private final INode ownerNode;
//    private final INode rawTypeNode;
//    private final List<INode> argumentNodes;
//    private final TypeGraph graph;
//
//    public PNodeV2(PType type,
//                   @Nullable INode ownerNode,
//                   INode rawTypeNode,
//                   List<INode> argumentNodes,
//                   TypeGraph graph) {
//        super(type);
//        this.ownerNode = ownerNode;
//        this.rawTypeNode = rawTypeNode;
//        this.argumentNodes = new ArrayList<>(argumentNodes);
//        this.graph = graph;
//    }
//
//    @Override
//    public PType getType() {
//        return (PType) super.getType();
//    }
//
////    @Override
////    protected boolean setLowerBoundMax0(IRType lowerBoundMax) {
////        return setOuterRange(lowerBoundMax);
////    }
//
////    @Override
////    protected boolean setUpperBoundMin0(IRType upperBoundMIn) {
////        return setOuterRange(upperBoundMIn);
////    }
//
////    @Override
////    protected boolean setUpperBoundMax0(IRType upperBound) {
////        if(upperBound instanceof PType pUpperBound) {
////            if(ownerNode != null
////                    && !ownerNode.setUpperBoundMax(NncUtils.requireNonNull(pUpperBound.getOwnerType()))) {
////                return false;
////            }
////            if(!rawTypeNode.setUpperBoundMax(pUpperBound.getRawType())) {
////                return false;
////            }
////            PType ancestor = (PType) IRUtil.getAncestorType(getType(), pUpperBound.getRawClass());
////            for (int i = 0; i < ancestor.getTypeArguments().size(); i++) {
////                graph.getNode(ancestor.getTypeArgument(i))
////                        .setOuterRange(pUpperBound.getTypeArgument(i));
////            }
////            return true;
////        }
////        else if(upperBound instanceof TypeIntersection intersection) {
////            return NncUtils.allMatch(intersection.getTypes(), this::setUpperBoundMax0);
////        }
////        else if(upperBound instanceof TypeUnion) {
////            return false;
////        }
////        else if(upperBound instanceof IRClass){
////            return rawTypeNode.setUpperBoundMax(upperBound);
////        }
////        else {
////            throw new InternalException("Invalid upper bound " + upperBound + " for PType");
////        }
////    }
//
////    @Override
////    protected boolean setLowerBoundMin0(IRType lowerBound) {
////        if(lowerBound instanceof PType pLowerBound) {
////            if(ownerNode != null
////                    && !ownerNode.setLowerBoundMin(NncUtils.requireNonNull(pLowerBound.getOwnerType()))) {
////                return false;
////            }
////            if(!rawTypeNode.setLowerBoundMin(pLowerBound.getRawType())) {
////                return false;
////            }
////            PType ancestor = (PType) IRUtil.getAncestorType(pLowerBound, getType().getRawClass());
////            for (int i = 0; i < argumentNodes.size(); i++) {
////                if(!argumentNodes.get(0).setInnerRange(ancestor.getTypeArgument(i))) {
////                    return false;
////                }
////            }
////            return true;
////        }
////        else {
////            return rawTypeNode.setLowerBoundMin(lowerBound);
////        }
////    }
//
//    @Override
//    public IRType getLowerBoundMax() {
//        var range = getOuterRange();
//        return range.isAtomic() ? range : null;
//    }
//
//    @Override
//    public IRType getUpperBoundMin() {
//        var range = getOuterRange();
//        return range.isAtomic() ? range : null;
//    }
//
//    @Override
//    public IRType getUpperBoundMax() {
//        return new PType(
//                NncUtils.get(ownerNode, INode::getUpperBoundMax),
//                rawTypeNode.getUpperBoundMax(),
//                NncUtils.map(argumentNodes, INode::getOuterRange)
//        );
//    }
//
//    @Override
//    public IRType getLowerBoundMin() {
//        return new PType(
//                NncUtils.get(ownerNode, INode::getLowerBoundMin),
//                rawTypeNode.getLowerBoundMin(),
//                NncUtils.map(argumentNodes, INode::getInnerRange)
//        );
//    }
//
//}
