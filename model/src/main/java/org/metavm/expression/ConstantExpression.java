package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Type;
import org.metavm.util.Constants;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType
public class ConstantExpression extends Expression {

    private final Value value;

    public ConstantExpression(@NotNull Value value) {
        this.value = value;
    }

    public Value getValue() {
        return value;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return switch (value) {
            case StringValue stringInstance -> "\"" + NncUtils.escape(stringInstance.getValue()) + "\"";
            case CharValue charValue -> "'" + NncUtils.escape(charValue.getValue()) + "'";
            case PrimitiveValue primitiveValue -> primitiveValue.getValue() + "";
            case Reference d -> {
                if (relaxedCheck)
                    yield  Constants.ID_PREFIX + NncUtils.orElse(d.getStringId(), "<uninitializedId>");
                else
                    yield  Constants.ID_PREFIX + NncUtils.requireNonNull(d.getStringId());
            }
            case null, default -> throw new InternalException("Invalid instance " + value);
        };
    }

    @Override
    public Type getType() {
        return value.getType();
    }

    @Override
    public List<Expression> getChildren() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return value;
    }

    public boolean isString() {
        return value instanceof StringValue;
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConstantExpression that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitConstantExpression(this);
    }

}
