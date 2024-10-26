package org.metavm.expression;

import org.metavm.entity.IEntityContext;
import org.metavm.flow.NodeRT;
import org.metavm.flow.ScopeRT;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.InstanceProvider;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ContextTypeDefRepository;
import org.metavm.object.type.IndexedTypeDefProvider;
import org.metavm.object.type.Type;
import org.metavm.util.IdentitySet;
import org.metavm.util.InternalException;

import java.util.*;

public class FlowParsingContext extends BaseParsingContext {

    public static FlowParsingContext create(NodeRT currentNode, IEntityContext entityContext) {
        return create(currentNode.getScope(), currentNode.getPredecessor(), entityContext);
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT prev, IEntityContext entityContext) {
        return new FlowParsingContext(
                entityContext.getInstanceContext(),
                new ContextTypeDefRepository(entityContext),
                prev);
    }

    private final NodeRT prev;
    private long lastBuiltVersion = -1L;
    private final Map<Id, NodeRT> id2node = new HashMap<>();
    private final Map<String, NodeRT> name2node = new HashMap<>();
    private final ExpressionTypeMap expressionTypes;

    public FlowParsingContext(
            InstanceProvider instanceProvider, IndexedTypeDefProvider typeDefProvider,
            NodeRT prev) {
        super(instanceProvider, typeDefProvider);
        this.prev = prev;
        this.expressionTypes = prev != null ? prev.getNextExpressionTypes() : ExpressionTypeMap.EMPTY;
    }

    @Override
    public Value getInstance(Id id) {
        return getInstanceProvider().get(id).getReference();
    }

    @Override
    public boolean isContextVar(Var var) {
        rebuildIfOutdated();
        return getNode(var) != null;
    }

    @Override
    public Expression resolveVar(Var var) {
        NodeRT node = getNode(var);
        if (node != null) {
            return new NodeExpression(node);
        }
        throw new InternalException(var + " is not a context var of " + this);
    }

    @Override
    public Type getExpressionType(Expression expression) {
        return expressionTypes.getType(expression);
    }

    @Override
    public Expression getDefaultExpr() {
        throw new InternalException("Flow context has no default context expression");
    }

    private void rebuildIfOutdated() {
        if (prev == null)
            return;
        if (lastBuiltVersion < prev.getFlow().getVersion())
            rebuild();
    }

    private void rebuild() {
        if (prev == null) return;
        id2node.clear();
        name2node.clear();
        Queue<NodeRT> queue = new LinkedList<>();
        queue.offer(prev);
        Set<NodeRT> visited = new IdentitySet<>();
        while (!queue.isEmpty()) {
            var node = queue.poll();
            visited.add(node);
            if (node.tryGetId() != null) {
                id2node.put(node.tryGetId(), node);
            }
            name2node.put(node.getName(), node);
            var prev = node.getPredecessor();
            if (prev != null && !visited.contains(prev)) {
                queue.offer(prev);
            }
        }
        lastBuiltVersion = prev.getFlow().getVersion();
    }

    private NodeRT getNode(Var var) {
        return switch (var.getType()) {
            case ID -> id2node.get(var.getId());
            case NAME -> name2node.get(var.getName());
        };
    }

}
