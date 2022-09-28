package tech.metavm.flow;

import tech.metavm.entity.Entity;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;

public abstract class NodeRT<P> extends Entity {
    private String name;
    private final NodeType type;
    private Type outputType;
    private final ScopeRT scope;
    private NodeRT<?> predecessor;
    private transient NodeRT<?> successor;

    public NodeRT(NodeDTO nodeDTO, Type outputType, ScopeRT scope) {
        this(
                nodeDTO.id(),
                nodeDTO.name(),
                NodeType.getByCodeRequired(nodeDTO.type()),
                outputType,
                NncUtils.get(nodeDTO.prevId(), scope::getNode),
                scope
        );
    }

    public NodeRT(NodePO nodePO, ScopeRT scope) {
        this(
                nodePO.getId(),
                nodePO.getName(),
                NodeType.getByCodeRequired(nodePO.getType()),
                NncUtils.get(nodePO.getOutputTypeId(), scope::getTypeFromContext),
                NncUtils.get(nodePO.getPrevId(), scope::getNode),
                scope
        );
    }

    protected NodeRT(
            Long id,
            String name,
            NodeType type,
            Type outputType,
            NodeRT<?> previous,
            ScopeRT scope
    ) {
        super(id, scope.getContext());
        setName(name);
        this.scope = scope;
        this.outputType = outputType;
        this.type = type;
        if(previous != null) {
            previous.insertAfter(this);
        }
        this.scope.addNode(this);
    }

    public FlowRT getFlow() {
        return scope.getFlow();
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public String getName() {
        return name;
    }

    public NodeType getType() {
        return type;
    }

    public NodeRT<?> getSuccessor() {
        return successor;
    }

    public NodeRT<?> getPredecessor() {
        return predecessor;
    }

    public void insertAfter(NodeRT<?> next) {
        if(this.successor != null) {
            next.successor = this.successor;
            this.successor.predecessor = next;
        }
        this.successor = next;
        next.predecessor = this;
    }

    public void remove() {
        if(this.predecessor != null) {
            this.predecessor.successor = this.successor;
        }
        if(this.successor != null) {
            this.successor.predecessor = this.predecessor;
        }
        this.predecessor = null;
        this.successor = null;
        scope.removeNode(this);
        context.remove(this);
    }

    public void update(NodeDTO nodeDTO) {
        setName(nodeDTO.name());
        setParam(nodeDTO.getParam());
    }

    protected void setOutputType(Type outputType) {
        this.outputType = outputType;
    }

    public final NodeDTO toDTO() {
        return new NodeDTO(
                id,
                getFlow().getId(),
                name,
                type.code(),
                NncUtils.get(predecessor, Entity::getId),
                NncUtils.get(outputType, Entity::getId),
                getParam(false),
                NncUtils.get(getOutputType(), Type::toDTO),
                scope.getId()
        );
    }

    public final NodePO toPO() {
        return new NodePO(
                id,
                getTenantId(),
                name,
                getFlow().getId(),
                type.code(),
                NncUtils.get(predecessor, Entity::getId),
                NncUtils.get(getOutputType(), Type::getId),
                scope.getId(),
                NncUtils.toJSONString(getParam(true)),
                0L
        );
    }

    public ScopeRT getScope() {
        return scope;
    }

    protected abstract P getParam(boolean forPersistence);

    protected abstract void setParam(P p);

    public NodeRT<?> getNodeFromContext(long nodeId) {
        return getFromContext(NodeRT.class, nodeId);
    }

    public FlowRT getFlowFromContext(long flowId) {
        return getFromContext(FlowRT.class, flowId);
    }

    public Type getOutputType() {
        return outputType;
    }

    public abstract void execute(FlowFrame frame);

}
