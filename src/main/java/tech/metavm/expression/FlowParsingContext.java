package tech.metavm.expression;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;

import java.util.*;

public class FlowParsingContext implements ParsingContext {

    public static FlowParsingContext create(NodeRT<?> currentNode, IInstanceContext instanceContext) {
        return new FlowParsingContext(currentNode.getGlobalPredecessors(), instanceContext);
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT<?> predecessor, IEntityContext entityContext) {
        return create(scope, predecessor, entityContext.getInstanceContext());
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT<?> predecessor, IInstanceContext instanceContext) {
        return new FlowParsingContext(
                predecessor != null ? predecessor : scope.getPredecessor(),
                instanceContext
        );
    }

    private final IInstanceContext instanceContext;
    private final List<NodeRT<?>> lastNodes;
    private long lastBuiltVersion = 0L;
    private final Map<Long, NodeRT<?>> id2node = new HashMap<>();
    private final Map<String, NodeRT<?>> name2node = new HashMap<>();
    private final Map<NodeRT<?>, NodeExpression> node2expression = new HashMap<>();

    public FlowParsingContext(NodeRT<?> lastNode, IInstanceContext instanceContext) {
        this(lastNode == null ? List.of() : List.of(lastNode), instanceContext);
    }

    public FlowParsingContext(List<NodeRT<?>> lastNodes, IInstanceContext instanceContext) {
        this.lastNodes = lastNodes == null ? new ArrayList<>() : new ArrayList<>(lastNodes);
        this.instanceContext = instanceContext;
    }

    @Override
    public Instance getInstance(long id) {
        return instanceContext.get(id);
    }

    @Override
    public boolean isContextVar(Var var) {
        rebuildIfOutdated();
        return getNode(var) != null;
    }

    @Override
    public Expression resolveVar(Var var) {
        NodeRT<?> node = getNode(var);
        if (node != null) {
            return node2expression.computeIfAbsent(node, NodeExpression::new);
        }
        throw new InternalException(var + " is not a context var of " + this);

    }

    @Override
    public Expression getDefaultExpr() {
        throw new InternalException("Flow context has no default context expression");
    }

    @Nullable
    @Override
    public IInstanceContext getInstanceContext() {
        return instanceContext;
    }

    private void rebuildIfOutdated() {
        if (lastNodes.isEmpty()) {
            return;
        }
        if (lastBuiltVersion < lastNodes.get(0).getFlow().getVersion()) {
            rebuild();
        }
    }

    private void rebuild() {
        if (lastNodes.isEmpty()) return;
        id2node.clear();
        name2node.clear();
        Queue<NodeRT<?>> queue = new LinkedList<>(lastNodes);
        Set<NodeRT<?>> visited = new IdentitySet<>();
        while (!queue.isEmpty()) {
            var node = queue.poll();
            visited.add(node);
            if (node.getId() != null) {
                id2node.put(node.getId(), node);
            }
            name2node.put(node.getName(), node);
            var predecessors = node.getGlobalPredecessors();
            for (NodeRT<?> pred : predecessors) {
                if (!visited.contains(pred)) queue.offer(pred);
            }
        }
        lastBuiltVersion = lastNodes.get(0).getFlow().getVersion();
    }

    private NodeRT<?> getNode(Var var) {
        return switch (var.getType()) {
            case ID -> id2node.get(var.getId());
            case NAME -> name2node.get(var.getName());
        };
    }

}
