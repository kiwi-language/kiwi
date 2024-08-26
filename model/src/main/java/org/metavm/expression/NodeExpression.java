package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.NodeRT;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType
public class NodeExpression extends Expression {

    private final NodeRT node;

    public NodeExpression(@NotNull NodeRT node) {
        this.node = node;
    }

    public NodeRT getNode() {
        return node;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return switch (symbolType) {
            case ID -> {
                try(var serContext = SerializeContext.enter()) {
                    yield idVarName(serContext.getId(node));
                }
            }
            case NAME -> node.getName();
        };
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return node.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return context.evaluate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeExpression that)) return false;
        return Objects.equals(node, that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNodeExpression(this);
    }
}
