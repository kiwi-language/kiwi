package tech.metavm.flow;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.expression.EvaluationContext;
import tech.metavm.flow.rest.ArgumentDTO;
import tech.metavm.object.instance.core.Instance;

import java.util.Objects;

@EntityType("实参")
public class Argument extends Element implements LocalKey {

    @ChildEntity("形参引用")
    private final ParameterRef parameterRef;
    @ChildEntity("值")
    private Value value;

    public Argument(Long tmpId, ParameterRef parameterRef, Value value) {
        super(tmpId);
        this.parameterRef = addChild(parameterRef.copy(), "parameterRef");
        this.value = addChild(value, "value");
    }

    public Parameter getParameter() {
        return parameterRef.resolve();
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
            return new ArgumentDTO(null, parameterRef.toDTO(serContext), value.toDTO());
        }
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArgument(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return parameterRef.getRawParameter().getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return Objects.requireNonNull(parameterRef.getRawParameter().getCode());
    }

    public String getText() {
        return getParameter().getName() + ": " + value.getText();
    }

}
