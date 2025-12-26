package org.metavm.expression;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Type;
import org.metavm.util.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@Wire
@Entity
public class ConstantExpression extends Expression {

    private final Value value;

    public ConstantExpression(@NotNull Value value) {
        this.value = value;
    }

    @Generated
    public static ConstantExpression read(MvInput input) {
        return new ConstantExpression(input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return switch (value) {
            case CharValue charValue -> "'" + Utils.escape(charValue.getValue()) + "'";
            case PrimitiveValue primitiveValue -> primitiveValue.getValue() + "";
            case StringReference s -> "\"" + Utils.escape(s.getValue()) + "\"";
            case EntityReference d -> {
                if (relaxedCheck)
                    yield  Constants.ID_PREFIX + Utils.orElse(d.getStringId(), "<uninitializedId>");
                else
                    yield  Constants.ID_PREFIX + Objects.requireNonNull(d.getStringId());
            }
            case null, default -> throw new InternalException("Invalid instance " + value);
        };
    }

    @Override
    public Type getType() {
        return value.getValueType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return value;
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof NativeValue t) t.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ConstantExpression);
        super.write(output);
        output.writeValue(value);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new ConstantExpression(value);
    }
}
