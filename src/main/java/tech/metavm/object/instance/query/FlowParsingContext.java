package tech.metavm.object.instance.query;

import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ScopeRT;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class FlowParsingContext implements ParsingContext {

    public static FlowParsingContext create(NodeRT<?> currentNode) {
        return new FlowParsingContext(/*currentNode.getInstanceContext(), */currentNode.getGlobalPredecessor());
    }

    public static FlowParsingContext create(ScopeRT scope, NodeRT<?> predecessor) {
        return new FlowParsingContext(
//                scope.getInstanceContext(),
                predecessor != null ?
                        predecessor
                        : NncUtils.get(scope.getOwner(), NodeRT::getGlobalPredecessor)
        );
    }

//    private final InstanceContext context;
    private final NodeRT<?> lastNode;
    private long lastBuiltVersion = 0L;
    private final Map<Long, NodeRT<?>> id2node = new HashMap<>();
    private final Map<String, NodeRT<?>> name2node = new HashMap<>();

    public FlowParsingContext(/*InstanceContext context, */NodeRT<?> lastNode) {
//        this.context = context;
        this.lastNode = lastNode;
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

//    @Override
//    public InstanceContext getInstanceContext() {
//        return context;
//    }

    private NodeRT<?> getNode(Var var) {
        return switch (var.getType()) {
            case ID -> id2node.get(var.getLongSymbol());
            case NAME -> name2node.get(var.getStringSymbol());
        };
    }

}
