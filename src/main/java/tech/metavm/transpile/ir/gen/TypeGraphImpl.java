package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.*;

import java.util.HashMap;
import java.util.Map;

public class TypeGraphImpl implements TypeGraph {

    private final Map<IRType, INode> nodeMap = new HashMap<>();
    private boolean solvable = true;

    @Override
    public boolean addRelation(TypeRelation relation) {
        var firstNode = node(relation.first());
        var secondNode = node(relation.second());
        var kind = relation.kind();
        if(applyRelation(firstNode, kind, secondNode)) {
            return true;
        }
        else {
            solvable = false;
            return false;
        }
    }

    private boolean applyRelation(INode first, RelationKind kind, INode second) {
        return switch (kind) {
//            case EQ -> first.addEqualNode(second);
            case EXTENDS -> first.addSuperNode(second);
            case SUPER -> first.addExtendingNode(second);
        };
    }

    private INode node(IRType type) {
        var node = nodeMap.get(type);
        if(node == null) {
            node = createNode(type);
            nodeMap.put(type, node);
        }
        return node;
    }

    @Override
    public INode getNode(IRType type) {
        return node(type);
    }

    private INode createNode(IRType type) {
        if(type.isConstant()) {
            return new ConstantNode(type, this);
        }
        return switch (type) {
            case PType pType -> new PNodeV3(pType, this);
            case TypeRange range -> new RangeNodeV3(range, this);
            case TypeUnion union -> new UnionNodeV3(union, this);
            case TypeIntersection intersection -> new IntersectionNode(intersection, this);
            case XType xType -> new XNode(xType, this);
            case null, default -> throw new RuntimeException("createNode not implemented yet for " + type);
        };
    }

    @Override
    public Map<XType, IRType> getSolution() {
        Map<XType, IRType> solution = new HashMap<>();
        for (INode node : nodeMap.values()) {
            if(node.getType() instanceof XType xType) {
                solution.put(xType, node.solve());
            }
        }
        return solution;
    }

    @Override
    public boolean isSolvable() {
        return solvable;
    }
}
