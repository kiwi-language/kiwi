package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.*;
import org.metavm.expression.EvaluationContext;
import org.metavm.flow.rest.ArgumentDTO;
import org.metavm.object.instance.core.Instance;

import java.util.Objects;

@EntityType
public class Argument extends Element implements LocalKey, org.metavm.entity.Value {

    private final ParameterRef parameterRef;
    private final Value value;

    public Argument(Long tmpId, ParameterRef parameterRef, Value value) {
        super(tmpId);
        this.parameterRef = parameterRef;
        this.value = value;
    }

    public Parameter getParameter() {
        return parameterRef.resolve();
    }

    public Value getValue() {
        return value;
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
