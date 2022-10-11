package tech.metavm.object.instance.query;

import tech.metavm.flow.NodeRT;
import tech.metavm.object.meta.Type;

public class NodeExpression extends Expression {

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
        return node.getOutputType();
    }

}
