package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.expression.FlowParsingContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ValueParamDTO;

@EntityType("计算节点")
public class ValueNode extends NodeRT<ValueParamDTO>  {

    public static ValueNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext context) {
        ValueParamDTO param = nodeDTO.getParam();
        var parsingContext = FlowParsingContext.create(scope, prev, context.getInstanceContext());
        var value = ValueFactory.create(param.value(), parsingContext);
        return new ValueNode(nodeDTO.tmpId(), nodeDTO.name(), prev, scope, value);
    }

    @ChildEntity("值")
    private Value value;

    public ValueNode(Long tmpId, String name, NodeRT<?> previous, ScopeRT scope, Value value) {
        super(tmpId, name, value.getType(), previous, scope);
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
            setOutputType(value.getType());
        }
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(value.evaluate(frame));
    }
}
