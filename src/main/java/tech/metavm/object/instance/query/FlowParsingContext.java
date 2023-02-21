package tech.metavm.object.instance.query;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.IInstanceContext;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlowParsingContext implements ParsingContext {

    public static FlowParsingContext create(NodeRT<?> currentNode, IInstanceContext instanceContext) {
        return new FlowParsingContext(currentNode.getGlobalPredecessor(), instanceContext);
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT<?> predecessor, IEntityContext entityContext) {
        return create(scope, predecessor, entityContext.getInstanceContext());
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT<?> predecessor, IInstanceContext instanceContext) {
        return new FlowParsingContext(
                predecessor != null ?
                        predecessor
                        : NncUtils.get(scope.getOwner(), NodeRT::getGlobalPredecessor),
                instanceContext
        );
    }

    private final IInstanceContext instanceContext;
    private final NodeRT<?> lastNode;
    private long lastBuiltVersion = 0L;
    private final Map<Long, NodeRT<?>> id2node = new HashMap<>();
    private final Map<String, NodeRT<?>> name2node = new HashMap<>();
    private final Map<NodeRT<?>, NodeExpression> node2expression = new HashMap<>();

    public FlowParsingContext(NodeRT<?> lastNode, IInstanceContext instanceContext) {
        this.lastNode = lastNode;
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
        if(node == null) {
            throw new InternalException(var + " is not a context var of " + this);
        }
        return node2expression.computeIfAbsent(node, NodeExpression::new);
    }


    @Override
    public Expression getDefaultExpr() {
        throw new InternalException("Flow context has no default context expression");
    }

    private void rebuildIfOutdated() {
        if(lastNode == null) {
            return;
        }
        if(lastBuiltVersion < lastNode.getFlow().getVersion()) {
            rebuild();
        }
    }

    private void rebuild() {
        id2node.clear();
        name2node.clear();
        NodeRT<?> node = this.lastNode;
        do {
            id2node.put(node.getId(), node);
            name2node.put(node.getName(), node);
            node = node.getGlobalPredecessor();
        } while(node != null && !id2node.containsKey(node.getId()));
        lastBuiltVersion = lastNode.getFlow().getVersion();
    }

    @Override
    public Expression parse(List<Var> varPath) {
        rebuildIfOutdated();
        NncUtils.requireMinimumSize(varPath, 1);
        NodeRT<?> node = getNode(varPath.get(0));
        Objects.requireNonNull(node);
        if(varPath.size() == 1) {
            return new NodeExpression(node);
        }
        else {
            List<Var> fieldPath = varPath.subList(1, varPath.size());
            List<Field> fields = TypeParsingContext.getFields((ClassType) node.getType(), fieldPath);
            return new FieldExpression(new NodeExpression(node), fields);
        }
    }

    private NodeRT<?> getNode(Var var) {
        return switch (var.getType()) {
            case ID -> id2node.get(var.getId());
            case NAME -> name2node.get(var.getName());
        };
    }

}
