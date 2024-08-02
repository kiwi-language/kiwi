package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.PrimitiveValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.StringValue;
import org.metavm.object.instance.core.Value;
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
        if(value instanceof StringValue stringInstance) {
            return "\"" + NncUtils.escape(stringInstance.getValue()) + "\"";
        }
        else if(value instanceof PrimitiveValue primitiveValue) {
            return primitiveValue.getValue() + "";
        }
        else if(value instanceof Reference d){
            if(relaxedCheck)
                return Constants.ID_PREFIX + NncUtils.orElse(d.getStringId(), "<uninitializedId>");
            else
                return Constants.ID_PREFIX + NncUtils.requireNonNull(d.getStringId());
        }
        else
            throw new InternalException("Invalid instance " + value);
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
