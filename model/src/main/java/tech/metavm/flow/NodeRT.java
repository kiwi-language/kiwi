package tech.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.ExpressionTypeMap;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.expression.ParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.NamingUtils;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@EntityType("节点")
public abstract class NodeRT extends Element implements LocalKey {

    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("编号")
    @Nullable
    private String code;
    @EntityField("类别")
    private final NodeKind kind;
    @ChildEntity("输出类型")
    @Nullable
    private Type outputType;
    @EntityField("所属范围")
    private final @NotNull ScopeRT scope;
    @EntityField("前驱")
    @Nullable
    private NodeRT predecessor;
    @EntityField("后继")
    @Nullable
    private NodeRT successor;
    @EntityField("错误")
    @Nullable
    private String error;

    private transient ExpressionTypeMap expressionTypes = ExpressionTypeMap.EMPTY;

    protected NodeRT(
            Long tmpId,
            @NotNull String name,
            @Nullable String code,
            @Nullable Type outputType,
            @Nullable NodeRT previous,
            @NotNull ScopeRT scope
    ) {
        super(tmpId);
        setName(name);
        this.code = NncUtils.get(code, NamingUtils::ensureValidCode);
        this.scope = scope;
        this.outputType = NncUtils.get(outputType, t -> addChild(t.copy(), "outputType"));
        this.kind = NodeKind.getByNodeClass(this.getClass());
        if (previous != null) {
            previous.insertAfter(this);
            setExpressionTypes(previous.getExpressionTypes());
        } else {
            setExpressionTypes(scope.getExpressionTypes());
        }
        this.scope.addNode(this);
    }

    @JsonIgnore
    public Flow getFlow() {
        return scope.getFlow();
    }

    public void setName(String name) {
        this.name = NamingUtils.ensureValidName(name);
    }

    public void setCode(@Nullable String code) {
        this.code = NncUtils.get(code, NamingUtils::ensureValidCode);
    }

    public String getName() {
        return name;
    }

    public NodeKind getKind() {
        return kind;
    }

    public @Nullable NodeRT getSuccessor() {
        return successor;
    }

    public NodeRT getDominator() {
        return predecessor != null ? predecessor : scope.getPredecessor();
    }

    public NodeRT getNext() {
        return successor != null ? successor : scope.getSuccessor();
    }

    public @Nullable NodeRT getPredecessor() {
        return predecessor;
    }

    void setSuccessor(@Nullable NodeRT node) {
        this.successor = node;
    }

    void setPredecessor(@Nullable NodeRT node) {
        this.predecessor = node;
    }

    public void insertAfter(NodeRT next) {
        if (this.successor != null) {
            next.setSuccessor(this.successor);
            this.successor.setPredecessor(next);
        }
        this.successor = next;
        next.setPredecessor(this);
    }

    public void insertBefore(NodeRT prev) {
        if (this.predecessor != null) {
            prev.setPredecessor(this.predecessor);
            this.predecessor.setSuccessor(prev);
        }
        this.predecessor = prev;
        prev.setSuccessor(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return code != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(code);
    }

    @Override
    public final List<Object> beforeRemove(IEntityContext context) {
        var cascade = new ArrayList<>(nodeBeforeRemove());
        if (kind.isOutputTypeAsChild() && outputType != null) {
            cascade.add(outputType);
        }
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

    public Callable getEnclosingCallable() {
        return accept(new EnclosingNodesVisitor<>() {

            @Override
            public Callable defaultValue() {
                return getFlow();
            }

            @Override
            public Callable visitNode(NodeRT node) {
                if (node instanceof Callable c)
                    return c;
                else
                    return super.visitNode(node);
            }
        });
    }

    protected List<Object> nodeBeforeRemove() {
        return List.of();
    }

    public ParsingContext getParsingContext(IEntityContext entityContext) {
        return FlowParsingContext.create(this, entityContext);
    }

    @Nullable
    public String getCode() {
        return code;
    }

    protected void setOutputType(@Nullable Type outputType) {
        this.outputType = NncUtils.get(outputType, t -> addChild(t, "outputType"));
    }

    public NodeDTO toDTO(SerializeContext serContext) {
        return new NodeDTO(
                serContext.getId(this),
                getFlow().getStringId(),
                name,
                code,
                kind.code(),
                NncUtils.get(predecessor, serContext::getId),
                NncUtils.get(getType(), serContext::getId),
                getParam(serContext),
                kind.isOutputTypeAsChild() ? getTypeDTO() : null,
                scope.getStringId(),
                error
        );
    }

    private TypeDTO getTypeDTO() {
        Type type = getType();
        if (type == null) {
            return null;
        }
        return type.toDTO();
    }

    public @NotNull ScopeRT getScope() {
        return scope;
    }

    public boolean isExit() {
        return false;
    }

    protected abstract Object getParam(SerializeContext serializeContext);

    public final void check() {
        setError(check0());
    }

    protected String check0() {
        return null;
    }

    @Nullable
    public String getError() {
        return error;
    }

    void setError(@Nullable String error) {
        this.error = error;
    }

    public @Nullable Type getType() {
        return outputType;
    }

    public abstract NodeExecResult execute(MetaFrame frame);

    public NodeExecResult next(Instance output) {
        return new NodeExecResult(output, null, getNext());
    }

    public NodeExecResult next() {
        return new NodeExecResult(null, null, getNext());
    }

    public ExpressionTypeMap getExpressionTypes() {
        return NncUtils.orElse(expressionTypes, () -> ExpressionTypeMap.EMPTY);
    }

    public void setExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = expressionTypes;
    }

    public void mergeExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = getExpressionTypes().merge(expressionTypes);
    }

    @Override
    protected String toString0() {
        return name;
    }

    public abstract void writeContent(CodeWriter writer);

    public void write(CodeWriter writer) {
        writer.writeNewLine(name + ": ");
        writeContent(writer);
    }

    public String getText() {
        CodeWriter writer = new CodeWriter();
        write(writer);
        return writer.toString();
    }

}
