package tech.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.autograph.ExpressionTypeMap;
import tech.metavm.autograph.TypeNarrower;
import tech.metavm.entity.*;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
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
    @EntityField("所属范围")
    private final ScopeRT scope;
    @EntityField("前驱")
    @Nullable
    private NodeRT<?> predecessor;
    @EntityField("后继")
    @Nullable
    private NodeRT<?> successor;

    private transient ExpressionTypeMap expressionTypes = ExpressionTypeMap.EMPTY;

    protected NodeRT(
            Long tmpId,
            String name,
            @Nullable Type outputType,
            NodeRT<?> previous,
            ScopeRT scope
    ) {
        super(tmpId);
        setName(name);
        this.scope = scope;
        this.outputType = outputType;
        this.kind = NodeKind.getByNodeClass(this.getClass());
        if (previous != null) {
            previous.insertAfter(this);
            setExpressionTypes(previous.getExpressionTypes());
        }
        else {
            setExpressionTypes(scope.getExpressionTypes());
        }
        this.scope.addNode(this);
    }

    @JsonIgnore
    public Flow getFlow() {
        return scope.getFlow();
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public String getName() {
        return name;
    }

    public NodeKind getKind() {
        return kind;
    }

    public @Nullable NodeRT<?> getSuccessor() {
        return successor;
    }

    public NodeRT<?> getDominator() {
        return predecessor != null ? predecessor : scope.getPredecessor();
    }

    public NodeRT<?> getNext() {
        return successor != null ? successor : scope.getSuccessor();
    }

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
        if (this.successor != null) {
            next.setSuccessor(this.successor);
            this.successor.setPredecessor(next);
        }
        this.successor = next;
        next.setPredecessor(this);
    }

    public void insertBefore(NodeRT<?> prev) {
        if(this.predecessor != null) {
            prev.setPredecessor(this.predecessor);
            this.predecessor.setSuccessor(prev);
        }
        this.predecessor = prev;
        prev.setSuccessor(this);
    }

    @Override
    public final List<Object> beforeRemove() {
        var cascade = nodeBeforeRemove();
        if (this.predecessor != null) {
            this.predecessor.setSuccessor(this.successor);
        }
        if (this.successor != null) {
            this.successor.setPredecessor(this.predecessor);
        }
        this.predecessor = null;
        this.successor = null;
        scope.removeNode(this);
        return cascade;
    }

    protected List<Object> nodeBeforeRemove() {
        return List.of();
    }

    public void update(NodeDTO nodeDTO, IEntityContext entityContext) {
        setName(nodeDTO.name());
        setParam(nodeDTO.getParam(), entityContext);
    }

    public ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(this, entityContext.getInstanceContext());
    }

    protected void setOutputType(@Nullable Type outputType) {
        this.outputType = outputType;
    }

    public final NodeDTO toDTO() {
        try (var context = SerializeContext.enter()) {
            return new NodeDTO(
                    context.getTmpId(this),
                    id,
                    getFlow().getId(),
                    name,
                    kind.code(),
                    NncUtils.get(predecessor, context::getRef),
                    NncUtils.get(outputType, context::getRef),
                    getParam(false),
                    getTypeDTO(),
                    scope.getId()
            );
        }
    }

    private TypeDTO getTypeDTO() {
        Type type = getType();
        if (type == null) {
            return null;
        }
        if (type instanceof ClassType classType) {
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

    public ScopeRT getScope() {
        return scope;
    }

    public boolean isExit() {
        return false;
    }

    protected abstract P getParam(boolean persisting);

    protected abstract void setParam(P param, IEntityContext context);

    public Type getType() {
        return outputType;
    }

    public abstract void execute(FlowFrame frame);

    public ExpressionTypeMap getExpressionTypes() {
        return NncUtils.orElse(expressionTypes, () -> ExpressionTypeMap.EMPTY);
    }

    public void setExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = expressionTypes;
    }

    public void mergeExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = getExpressionTypes().merge(expressionTypes);
    }

}
