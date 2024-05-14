package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.LoopNode;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceProvider;
import tech.metavm.object.type.ContextTypeDefRepository;
import tech.metavm.object.type.IndexedTypeDefProvider;
import tech.metavm.object.type.Type;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;

import java.util.*;

public class FlowParsingContext extends BaseParsingContext {

    public static FlowParsingContext create(NodeRT currentNode, IEntityContext entityContext) {
        return create(currentNode.getScope(), currentNode.getPredecessor(), entityContext);
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT prev, IEntityContext entityContext) {
        return new FlowParsingContext(
                entityContext.getInstanceContext(),
                new ContextTypeDefRepository(entityContext),
                scope, prev);
    }

    private final ScopeRT scope;
    private final NodeRT prev;
    private final NodeRT lastNode;
    private long lastBuiltVersion = -1L;
    private final Map<Id, NodeRT> id2node = new HashMap<>();
    private final Map<String, NodeRT> name2node = new HashMap<>();
    private final Map<NodeRT, NodeExpression> node2expression = new HashMap<>();

    public FlowParsingContext(
            InstanceProvider instanceProvider, IndexedTypeDefProvider typeDefProvider,
            ScopeRT scope, NodeRT prev) {
        super(instanceProvider, typeDefProvider);
        this.scope = scope;
        this.prev = prev;
        this.lastNode = prev != null ? prev : scope.getOwner();
    }

    private @javax.annotation.Nullable Expression getScopeCondition(ScopeRT scope) {
        if(scope.getBranch() != null) {
            return scope.getBranch().getCondition().getExpression();
        }
        else if(scope.getOwner() instanceof LoopNode loopNode) {
            return loopNode.getCondition().getExpression();
        }
        else {
            return null;
        }
    }

    @Override
    public Instance getInstance(Id id) {
        return getInstanceProvider().get(id);
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
        return prev != null ? prev.getExpressionTypes().getType(expression) :
                scope.getExpressionTypes().getType(expression);
    }

    @Override
    public Expression getDefaultExpr() {
        throw new InternalException("Flow context has no default context expression");
    }

    private void rebuildIfOutdated() {
        if (lastNode == null)
            return;
        if (lastBuiltVersion < lastNode.getFlow().getVersion())
            rebuild();
    }

    private void rebuild() {
        if (lastNode == null) return;
        id2node.clear();
        name2node.clear();
        Queue<NodeRT> queue = new LinkedList<>();
        queue.offer(lastNode);
        Set<NodeRT> visited = new IdentitySet<>();
        while (!queue.isEmpty()) {
            var node = queue.poll();
            visited.add(node);
            if (node.tryGetId() != null) {
                id2node.put(node.tryGetId(), node);
            }
            name2node.put(node.getName(), node);
            var dom = node.getDominator();
            if (dom != null && !visited.contains(dom)) {
                queue.offer(dom);
            }
        }
        lastBuiltVersion = lastNode.getFlow().getVersion();
    }

    private NodeRT getNode(Var var) {
        return switch (var.getType()) {
            case ID -> id2node.get(var.getId());
            case NAME -> name2node.get(var.getName());
        };
    }

}
