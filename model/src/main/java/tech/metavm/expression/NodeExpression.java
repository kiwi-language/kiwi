package tech.metavm.expression;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.NodeRT;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@EntityType("节点表达式")
public class NodeExpression extends Expression {

    @EntityField("节点")
    private final NodeRT node;

    public NodeExpression(@NotNull NodeRT node) {
        this.node = node;
    }

    public NodeRT getNode() {
        return node;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return switch (symbolType) {
            case ID -> idVarName(node.tryGetId());
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
    protected Instance evaluateSelf(EvaluationContext context) {
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
