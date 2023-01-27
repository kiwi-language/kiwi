package tech.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.query.FlowParsingContext;
import tech.metavm.object.instance.query.ParsingContext;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.NameUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

import java.util.List;

import static tech.metavm.util.ContextUtil.getTenantId;

@EntityType("节点")
public abstract class NodeRT<P> extends Entity {

    @EntityField("名称")
    private String name;
    @EntityField("类别")
    private final NodeKind kind;
    @EntityField("输出类型")
    @Nullable
    private Type outputType;
    @EntityField("范围")
    private final ScopeRT scope;
    @EntityField("前驱")
    @Nullable
    private NodeRT<?> predecessor;
    @EntityField("后继")
    @Nullable
    private NodeRT<?> successor;

    private transient ParsingContext parsingContext;

    public NodeRT(NodeDTO nodeDTO, Type outputType, ScopeRT scope) {
        this(
                nodeDTO.name(),
                NodeKind.getByCodeRequired(nodeDTO.type()),
                outputType,
                NncUtils.get(nodeDTO.prevId(), scope::getNode),
                scope
        );
    }

    protected NodeRT(
            String name,
            NodeKind kind,
            @Nullable Type outputType,
            NodeRT<?> previous,
            ScopeRT scope
    ) {
//        super(scope.getContext());
        setName(name);
        this.scope = scope;
        this.outputType = outputType;
        this.kind = kind;
        if(previous != null) {
            previous.insertAfter(this);
        }
        this.scope.addNode(this);
    }

    @JsonIgnore
    public FlowRT getFlow() {
        return scope.getFlow();
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public String getName() {
        return name;
    }

    public NodeKind getNodeType() {
        return kind;
    }

    @JsonIgnore
    public @Nullable NodeRT<?> getSuccessor() {
        return successor;
    }

    @JsonIgnore
    public NodeRT<?> getGlobalPredecessor() {
        if(predecessor != null) {
            return predecessor;
        }
        return NncUtils.get(scope.getOwner(), NodeRT::getGlobalPredecessor);
    }

    @JsonIgnore
    public NodeRT<?> getGlobalSuccessor() {
        if(successor != null) {
            return successor;
        }
        return NncUtils.get(scope.getOwner(), NodeRT::getGlobalSuccessor);
    }

    @JsonIgnore
    public @Nullable NodeRT<?> getPredecessor() {
        return predecessor;
    }

    void setSuccessor(@Nullable NodeRT<?> node) {
        this.successor = node;
    }

    void setPredecessor(@Nullable NodeRT<?> node) {
        this.predecessor = node;
    }

    public void insertAfter(NodeRT<?> next) {
        if(this.successor != null) {
            next.setSuccessor(this.successor);
            this.successor.setPredecessor(next);
        }
        this.successor = next;
        next.setPredecessor(this);
    }

    @Override
    public List<Object> beforeRemove() {
        if(this.predecessor != null) {
            this.predecessor.setSuccessor(this.successor);
        }
        if(this.successor != null) {
            this.successor.setPredecessor(this.predecessor);
        }
        this.predecessor = null;
        this.successor = null;
        scope.removeNode(this);
        return List.of();
    }

    public void update(NodeDTO nodeDTO, IEntityContext entityContext) {
        setName(nodeDTO.name());
        setParam(nodeDTO.getParam(), entityContext);
    }

    @JsonIgnore
    public final ParsingContext getParsingContext(IEntityContext entityContext) {
        return parsingContext = FlowParsingContext.create(this, entityContext.getInstanceContext());
    }

    protected void setOutputType(ClassType outputType) {
        this.outputType = outputType;
    }

    public final NodeDTO toDTO() {
        return new NodeDTO(
                id,
                getFlow().getId(),
                name,
                kind.code(),
                NncUtils.get(predecessor, Entity::getId),
                NncUtils.get(outputType, Entity::getId),
                getParam(false),
                getTypeDTO(),
                scope.getId()
        );
    }

    private TypeDTO getTypeDTO() {
        Type type = getType();
        if(type == null) {
            return null;
        }
        if(type instanceof ClassType classType) {
            return classType.toDTO(true, true);
        }
        return type.toDTO();
    }

    public final NodePO toPO() {
        return new NodePO(
                id,
                getTenantId(),
                name,
                getFlow().getId(),
                kind.code(),
                NncUtils.get(predecessor, Entity::getId),
                NncUtils.get(getType(), Type::getId),
                scope.getId(),
                NncUtils.toJSONString(getParam(true)),
                0L
        );
    }

    @JsonIgnore
    public ScopeRT getScope() {
        return scope;
    }

    protected abstract P getParam(boolean persisting);

    protected abstract void setParam(P p, IEntityContext context);

    @JsonIgnore
    public Type getType() {
        return outputType;
    }

    public abstract void execute(FlowFrame frame);

}
