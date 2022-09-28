package tech.metavm.object.instance.query;

import tech.metavm.flow.NodeRT;

public class NodeFieldExpression extends Expression {

    private final NodeRT<?> node;
    private final FieldExpression fieldExpression;

    public NodeFieldExpression(NodeRT<?> node, FieldExpression fieldExpression) {
        this.node = node;
        this.fieldExpression = fieldExpression;
    }

    public NodeRT<?> getNode() {
        return node;
    }

    public FieldExpression getFieldExpression() {
        return fieldExpression;
    }
}
