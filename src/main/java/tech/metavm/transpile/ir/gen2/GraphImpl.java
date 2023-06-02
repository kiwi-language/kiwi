package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.IRWildCardType;
import tech.metavm.transpile.ir.PType;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GraphImpl implements Graph {

    private final Map<IRType, Node> nodeMap = new HashMap<>();
    private boolean solvable;

    public GraphImpl(boolean solvable) {
        this.solvable = solvable;
    }

    public GraphImpl() {
        this(true);
    }

    public GraphImpl(Graph graph) {
        this.solvable = graph.isSolvable();
        merge(graph);
    }

    @Override
    public boolean addExtension(IRType extendingType, IRType superType) {
        if(addExtension0(extendingType, superType)) {
            return true;
        }
        else {
            if(solvable) {
                solvable = false;
            }
            return false;
        }
    }

    @Override
    public Collection<Node> getNodes() {
        return Collections.unmodifiableCollection(nodeMap.values());
    }

    @Override
    public void merge(Graph graph) {
        graph.getNodes().forEach(this::copyNode);
    }

    private boolean addExtension0(IRType extendingType, IRType superType) {
        if(superType.isVariable() && extendingType.isVariable()) {
            if(extendingType instanceof XType xType) {
                return getXNode(xType).addSuperNode(getNode(superType));
            }
            else {
                throw new InternalException("Invalid extension from " + superType + " to " + extendingType);
            }
        }
        else if(superType.isVariable()) {
            return getNode(superType).addLowerBound(extendingType);
        }
        else if(extendingType.isVariable()) {
            return getNode(extendingType).addUpperBound(superType);
        }
        else {
            return superType.isAssignableFrom(extendingType);
        }
    }

    private XNode getXNode(XType xType) {
        return (XNode) getNode(xType);
    }

    @Override
    public Node getNode(IRType type) {
        var node = nodeMap.get(type);
        if(node != null) {
            return node;
        }
        node = createNode(type);
        nodeMap.put(type, node);
        return node;
    }

    @Override
    public Map<XType, IRType> getSolution() {
        var xNodes = NncUtils.filterByType(nodeMap.values(), XNode.class);
        return NncUtils.toMap(xNodes, XNode::getType, XNode::solve);
    }

    @Override
    public boolean isSolvable() {
        return solvable;
    }

    private Node createNode(IRType type) {
        return switch (type) {
            case PType pType -> new PNode(pType, this);
            case XType xType -> new XNode(xType);
            case IRWildCardType wct -> new RNode(wct, this);
            default -> new CNode(type);
        };
    }


    public Node copyNode(Node node) {
        var existing = nodeMap.get(node.getType());
        if(existing != null) {
            return existing;
        }
        var nodeCopy = node.copy(this);
        nodeMap.put(nodeCopy.getType(), nodeCopy);
        return nodeCopy;
    }

}
