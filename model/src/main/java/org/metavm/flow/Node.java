package org.metavm.flow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Element;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.LocalKey;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@EntityType
public abstract class Node extends Element implements LocalKey {

    @EntityField(asTitle = true)
    private final String name;
    private @Nullable Type outputType;
    private final @NotNull Code code;
    @Nullable
    private Node predecessor;
    @Nullable
    private Node successor;
    @Nullable
    private String error;
    private transient ExpressionTypeMap expressionTypes;
    private transient int offset;

    protected Node(
            Long tmpId,
            @NotNull String name,
            @Nullable Type outputType,
            @Nullable Node previous,
            @NotNull Code code
    ) {
        super(tmpId);
        this.name = name;
        this.code = code;
        this.outputType = outputType;
        if (previous != null)
            previous.insertAfter(this);
        if (previous != null && previous.isSequential())
            setExpressionTypes(previous.getNextExpressionTypes());
        this.code.addNode(this);
    }

    @JsonIgnore
    public Flow getFlow() {
        return code.getFlow();
    }

    public String getName() {
        return name;
    }

    public @Nullable Node getSuccessor() {
        return successor;
    }

    public @Nullable Node getPredecessor() {
        return predecessor;
    }

    void setSuccessor(@Nullable Node node) {
        this.successor = node;
    }

    void setPredecessor(@Nullable Node node) {
        this.predecessor = node;
    }

    public void insertAfter(Node next) {
        if (this.successor != null) {
            next.setSuccessor(this.successor);
            this.successor.setPredecessor(next);
        }
        this.successor = next;
        next.setPredecessor(this);
    }

    public void insertBefore(Node prev) {
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
        code.removeNode(this);
        return cascade;
    }

    protected List<Object> nodeBeforeRemove() {
        return List.of();
    }

    protected void setOutputType(@Nullable Type outputType) {
        this.outputType = outputType;
    }

    public @NotNull Code getCode() {
        return code;
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

    public ExpressionTypeMap getExpressionTypes() {
        return NncUtils.orElse(expressionTypes, () -> ExpressionTypeMap.EMPTY);
    }

    public void setExpressionTypes(ExpressionTypeMap expressionTypes) {
        this.expressionTypes = expressionTypes;
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

    public abstract void writeCode(CodeOutput output);

    public abstract int getLength();

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}