package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.object.instance.core.Instance;

import java.util.Objects;

@EntityType("实参")
public class Argument extends Element implements LocalKey {

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
        try(var serContext = SerializeContext.enter()) {
            return new ArgumentDTO(null, serContext.getId(parameter), value.toDTO());
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArgument(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return parameter.getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(parameter.getCode());
    }

    public String getText() {
        return getParameter().getName() + ": " + value.getText();
    }

}
