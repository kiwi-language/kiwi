package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ElementVisitor;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueParamDTO;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

@EntityType("计算节点")
public class ValueNode extends NodeRT<ValueParamDTO>  {

    public static ValueNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        ValueParamDTO param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context.getInstanceContext());
        var value = ValueFactory.create(param.value(), parsingContext);
        var outputType = parsingContext.getExpressionType(value.getExpression()).getCertainUpperBound();
        return new ValueNode(nodeDTO.tmpId(), nodeDTO.name(), outputType, prev, scope, value);
    }

    @ChildEntity("值")
    private Value value;

    public ValueNode(Long tmpId, String name, Type outputType, NodeRT<?> previous, ScopeRT scope, Value value) {
        super(tmpId, name, outputType, previous, scope);
        this.value = value;
    }

    @Override
    protected ValueParamDTO getParam(boolean persisting) {
        return new ValueParamDTO(value.toDTO(persisting));
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    protected void setParam(ValueParamDTO param, IEntityContext context) {
        if(param.value() != null) {
            value = ValueFactory.create(param.value(), getParsingContext(context));
            setOutputType(value.getType().getCertainUpperBound());
        }
    }

    @Override
    @NotNull
    public Type getType() {
        return NncUtils.requireNonNull(super.getType());
    }

    @Override
    public void execute(MetaFrame frame) {
        frame.setResult(value.evaluate(frame));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitValueNode(this);
    }
}
