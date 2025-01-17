package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.NodeExpression;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.Objects;
import java.util.function.Consumer;

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
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
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

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("node", this.getNode().getStringId());
        map.put("text", this.getText());
        map.put("expression", this.getExpression().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_NodeValue);
        super.write(output);
        output.writeValue(node);
    }
}
