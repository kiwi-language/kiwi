package tech.metavm.flow;

import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.object.instance.core.Instance;

@EntityType("实参")
public class Argument extends Element {

    @EntityField("形参")
    private final Parameter parameter;
    @ChildEntity("值")
    private Value value;

    public Argument(Long tmpId, Parameter parameter, Value value) {
        super(tmpId);
        this.parameter = parameter;
        this.value = addChild(value, "value");
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = addChild(value, "value");
    }

    public Instance evaluate(EvaluationContext evaluationContext) {
        return value.evaluate(evaluationContext);
    }

    public ArgumentDTO toDTO() {
        try(var context = SerializeContext.enter()) {
            return new ArgumentDTO(null, context.getRef(parameter), value.toDTO(false));
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArgument(this);
    }
}
