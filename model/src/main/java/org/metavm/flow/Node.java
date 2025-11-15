package org.metavm.flow;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.Ref;
import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.entity.BuildKeyContext;
import org.metavm.entity.Element;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.LocalKey;
import org.metavm.expression.ExpressionTypeMap;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.InstanceState;
import org.metavm.object.instance.core.NativeEphemeralObject;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Slf4j
@Entity
public abstract class Node implements LocalKey, Element, NativeEphemeralObject {

    @Getter
    @EntityField(asTitle = true)
    private final String name;
    private @Nullable Type outputType;
    @Ref
    private final @NotNull Code code;
    @Ref
    @Nullable
    private Node predecessor;
    @Ref
    @Nullable
    private Node successor;
    @Nullable
    private String error;
    @Setter
    private transient ExpressionTypeMap expressionTypes;
    @Getter
    @Setter
    private transient int offset;
    private final transient InstanceState state = InstanceState.ephemeral(this);

    protected Node(
            @NotNull String name,
            @Nullable Type outputType,
            @Nullable Node previous,
            @NotNull Code code
    ) {
        this.name = name;
        this.code = code;
        this.outputType = outputType;
        if (previous != null)
            previous.insertAfter(this);
        if (previous != null && previous.isSequential())
            setExpressionTypes(previous.getNextExpressionTypes());
        this.code.addNode(this);
    }

    public Flow getFlow() {
        return code.getFlow();
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

    public abstract boolean hasOutput();

    public ExpressionTypeMap getExpressionTypes() {
        return Utils.orElse(expressionTypes, () -> ExpressionTypeMap.EMPTY);
    }

    @Override
    public String toString() {
        return name;
    }

    public abstract void writeContent(CodeWriter writer);

    public void write(CodeWriter writer) {
        try {
            writer.write(name + ": ");
            writeContent(writer);
            writer.writeln();
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to write content of node " + name, e);
        }
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

    @Override
    public String getTitle() {
        return getName();
    }

    @Override
    public InstanceState state() {
        return state;
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        if (outputType != null) outputType.accept(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        if (outputType != null) outputType.forEachReference(action);
        code.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }
}