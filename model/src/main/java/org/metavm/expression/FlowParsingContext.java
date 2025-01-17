package org.metavm.expression;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.flow.Node;
import org.metavm.flow.Code;
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

    public static FlowParsingContext create(Node currentNode, IInstanceContext entityContext) {
        return create(currentNode.getCode(), currentNode.getPredecessor(), entityContext);
    }

    public static FlowParsingContext create(Code code, Node prev, IInstanceContext entityContext) {
        return new FlowParsingContext(
                entityContext,
                new ContextTypeDefRepository(entityContext),
                prev);
    }

    private final Node prev;
    private long lastBuiltVersion = -1L;
    private final Map<Id, Node> id2node = new HashMap<>();
    private final Map<String, Node> name2node = new HashMap<>();
    private final ExpressionTypeMap expressionTypes;

    public FlowParsingContext(
            InstanceProvider instanceProvider, IndexedTypeDefProvider typeDefProvider,
            Node prev) {
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
        Node node = getNode(var);
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
        Queue<Node> queue = new LinkedList<>();
        queue.offer(prev);
        Set<Node> visited = new IdentitySet<>();
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

    public Node getNodeById(Id id) {
        rebuildIfOutdated();
        return Objects.requireNonNull(id2node.get(id), () -> "Cannot find node with ID " + id);
    }

    private Node getNode(Var var) {
        return switch (var.getType()) {
            case ID -> id2node.get(var.getId());
            case NAME -> name2node.get(var.getName());
        };
    }

}
