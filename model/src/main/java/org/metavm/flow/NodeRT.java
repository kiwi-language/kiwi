package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.object.type.Type;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EntityType
public abstract class NodeRT extends Element implements LocalKey {

    @EntityField(asTitle = true)
    private final String name;
    private final NodeKind kind;
    private @Nullable Type outputType;
    private final @NotNull ScopeRT scope;
    @Nullable
    private NodeRT predecessor;
    @Nullable
    private NodeRT successor;
    @Nullable
    private String error;

    private transient ExpressionTypeMap expressionTypes;

    protected NodeRT(
            Long tmpId,
            @NotNull String name,
            @Nullable Type outputType,
            @Nullable NodeRT previous,
            @NotNull ScopeRT scope
    ) {
        super(tmpId);
        this.name = name;
        this.scope = scope;
        this.outputType = outputType;
        this.kind = NodeKind.fromNodeClass(this.getClass());
        if (previous != null)
            previous.insertAfter(this);
        if (previous != null && previous.isSequential())
            setExpressionTypes(previous.getNextExpressionTypes());
        this.scope.addNode(this);
    }

    @JsonIgnore
    public Flow getFlow() {
        return scope.getFlow();
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
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    @Override
    public final List<Object> beforeRemove(IEntityContext context) {
        var cascade = new ArrayList<>(nodeBeforeRemove());
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

    protected void setOutputType(@Nullable Type outputType) {
        this.outputType = outputType;
    }

    public NodeDTO toDTO(SerializeContext serContext) {
        return new NodeDTO(
                serContext.getStringId(this),
                getFlow().getStringId(),
                name,
                kind.code(),
                NncUtils.get(predecessor, serContext::getStringId),
                NncUtils.get(getType(), t -> t.toExpression(serContext)),
                getParam(serContext),
                getOutputKlassDTO(),
                scope.getStringId(),
                error
        );
    }

    protected KlassDTO getOutputKlassDTO() {
        return null;
    }

    public @NotNull ScopeRT getScope() {
        return scope;
    }

    public boolean isExit() {
        return false;
    }

    public boolean isUnconditionalJump() {
        return false;
    }

    public boolean isSequential() {
        return !isExit() && !isUnconditionalJump();
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

    public abstract int execute(MetaFrame frame);

    public ExpressionTypeMap getExpressionTypes() {
        return NncUtils.orElse(expressionTypes, () -> ExpressionTypeMap.EMPTY);
    }

    public void setExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = expressionTypes;
    }

    public void mergeExpressionTypes(ExpressionTypeMap expressionTypes) {
        if (this.expressionTypes == null)
            this.expressionTypes = expressionTypes;
        else
            this.expressionTypes = this.expressionTypes.merge(expressionTypes);
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

    public ExpressionTypeMap getNextExpressionTypes() {
        return getExpressionTypes();
    }

    public abstract int getStackChange();

}