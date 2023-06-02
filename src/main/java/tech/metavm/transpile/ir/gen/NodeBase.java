package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class NodeBase implements INode {

    private final IRType type;
    protected final TypeGraph graph;
    private final List<NodeListener> listeners = new ArrayList<>();

    protected NodeBase(IRType type, TypeGraph graph) {
        this.type = type;
        this.graph = graph;
    }

    @Override
    public boolean addSuperNode(INode superNode) {
        return le(superNode) && superNode.ge(this);
    }

    @Override
    public boolean addExtendingNode(INode node) {
        return node.addSuperNode(this);
    }

    protected final boolean onChange() {
        return NncUtils.allMatch(listeners, NodeListener::onChange);
    }

    @Override
    public final void addListener(NodeListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public IRType getType() {
        return type;
    }

    public TypeGraph getGraph() {
        return graph;
    }
}
