package tech.metavm.expression;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.NodeRT;
import tech.metavm.object.meta.Type;

import java.util.List;

@EntityType("节点表达式")
public class NodeExpression extends Expression {

    @EntityField("节点")
    private final NodeRT<?> node;

    public NodeExpression(NodeRT<?> node) {
        this.node = node;
    }

    public NodeRT<?> getNode() {
        return node;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return switch (symbolType) {
            case ID -> idVarName(node.getId());
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
    protected List<Expression> getChildren() {
        return List.of();
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new NodeExpression(node);
    }

}
