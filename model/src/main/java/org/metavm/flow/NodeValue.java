package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.NodeExpression;
import org.metavm.object.type.Type;

import java.util.Objects;

public class NodeValue extends Value {

    private final Node node;

    public NodeValue(@NotNull Node node) {
        this.node = node;
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Type getType() {
        return Objects.requireNonNull(node.getType());
    }

    @Override
    public String getText() {
        return node.getName();
    }

    @Override
    public Expression getExpression() {
        return new NodeExpression(node);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNodeValue(this);
    }
}
