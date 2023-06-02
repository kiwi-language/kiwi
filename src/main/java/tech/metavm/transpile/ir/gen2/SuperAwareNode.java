package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ir.IRType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class SuperAwareNode extends Node {

    private final Set<Node> superNodes;

    protected SuperAwareNode(IRType type, Set<Node> superNodes) {
        super(type);
        this.superNodes = new HashSet<>(superNodes);
    }

    public SuperAwareNode(IRType type) {
        this(type, new HashSet<>());
    }

    public final boolean addSuperNode(Node node) {
        superNodes.add(node);
        return onSuperNodeAdded(node);
    }

    public Set<Node> getSuperNodes() {
        return Collections.unmodifiableSet(superNodes);
    }

    protected abstract boolean onSuperNodeAdded(Node node);

}
