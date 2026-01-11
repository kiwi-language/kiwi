package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.expression.EvaluationContext;
import org.manul.expression.Expression;
import org.manul.expression.NodeExpression;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.Objects;
import java.util.function.Consumer;

@Wire
public class NodeValue extends Value {

    private final Reference node;

    public NodeValue(@NotNull Node node) {
        this.node = node.getReference();
    }

    public NodeValue(Reference node) {
        this.node = node;
    }

    @Generated
    public static NodeValue read(MvInput input) {
        return new NodeValue((Reference) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    public org.manul.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType() {
        return Objects.requireNonNull(node.getValueType());
    }

    public Node getNode() {
        return (Node) node.get();
    }

    @Override
    public String getText() {
        return getNode().getName();
    }

    @Override
    public Expression getExpression() {
        return new NodeExpression(getNode());
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        action.accept(node);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_NodeValue);
        super.write(output);
        output.writeValue(node);
    }
}
