package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.*;
import org.metavm.expression.FlowParsingContext;
import org.metavm.flow.rest.NodeDTO;
import org.metavm.flow.rest.ValueNodeParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType
public class ValueNode extends NodeRT {

    public static ValueNode save(NodeDTO nodeDTO, NodeRT prev, ScopeRT scope, IEntityContext context) {
        ValueNodeParam param = nodeDTO.getParam();
        var node = (ValueNode) context.getNode(Id.parse(nodeDTO.id()));
        var parsingContext = FlowParsingContext.create(scope, prev, context);
        var value = ValueFactory.create(param.value(), parsingContext);
        var outputType = parsingContext.getExpressionType(value.getExpression()).getCertainUpperBound();
        if (node == null)
            node = new ValueNode(nodeDTO.tmpId(), nodeDTO.name(), nodeDTO.code(), outputType, prev, scope, value);
        else {
            node.setValue(value);
            node.setOutputType(outputType);
        }
        return node;
    }

    private Value value;

    public ValueNode(Long tmpId, String name, @Nullable String code, Type outputType, NodeRT previous, ScopeRT scope, Value value) {
        super(tmpId, name, code, outputType, previous, scope);
        this.value = value;
    }

    @Override
    protected ValueNodeParam getParam(SerializeContext serializeContext) {
        return new ValueNodeParam(value.toDTO());
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    @NotNull
    public Type getType() {
        return NncUtils.requireNonNull(super.getType());
    }

    @Override
    public NodeExecResult execute(MetaFrame frame) {
        return next(value.evaluate(frame));
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("value " + value.getText());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitValueNode(this);
    }
}
