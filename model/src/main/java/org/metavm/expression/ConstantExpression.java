package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class ConstantExpression extends Expression {

    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
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

    public Value getValue() {
        return value;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return switch (value) {
            case StringValue stringInstance -> "\"" + Utils.escape(stringInstance.getValue()) + "\"";
            case CharValue charValue -> "'" + Utils.escape(charValue.getValue()) + "'";
            case PrimitiveValue primitiveValue -> primitiveValue.getValue() + "";
            case Reference d -> {
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

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof NativeValue t) t.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("value", this.getValue().toJson());
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("string", this.isString());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ConstantExpression);
        super.write(output);
        output.writeValue(value);
    }
}
