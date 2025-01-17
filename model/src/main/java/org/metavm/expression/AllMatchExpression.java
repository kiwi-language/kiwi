package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.PrimitiveType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class AllMatchExpression extends Expression {

    private final Expression array;
    private final Expression condition;

    public AllMatchExpression(@NotNull Expression array, @NotNull Expression condition) {
        this.array = array;
        this.condition = condition;
    }

    @Generated
    public static AllMatchExpression read(MvInput input) {
        return new AllMatchExpression(Expression.read(input), Expression.read(input));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        Expression.visit(visitor);
    }

    public Expression getArray() {
        return array;
    }

    public Expression getCondition() {
        return condition;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "allmatch(" +
                getArray().build(symbolType, false, relaxedCheck) + ", "
                + condition.build(symbolType, false, relaxedCheck)
                + ")";
    }

    public ArrayType getArrayType() {
        return (ArrayType) getArray().getType();
    }

    @Override
    public int precedence() {
        return 10;
    }

    @Override
    public Type getType() {
        return PrimitiveType.booleanType;
    }

    public @Nullable CursorExpression createCursor() {
        return new CursorExpression(getArrayType().getElementType(), getCursorAlias());
    }

    @Nullable
    public String getCursorAlias() {
        return array instanceof AsExpression asExpression ? asExpression.getAlias() : null;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(array, condition);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        Value instance = array.evaluate(context);
        if (instance.isNull()) {
            return Instances.trueInstance();
        }
        if (!(instance instanceof Reference r && r.get() instanceof ArrayInstance)) {
            throw new InternalException("Expecting array instance for AllMatchExpression but got " + instance);
        }
        for (Value element : instance.resolveArray().getElements()) {
            if (element instanceof Reference r1) {
                EvaluationContext subContext = new SubEvaluationContext(context, r1);
                if (!Instances.isTrue(condition.evaluate(subContext))) {
                    return Instances.falseInstance();
                }
            } else {
                throw new InternalException("AllMatchExpression only supports reference array right now");
            }
        }
        return Instances.trueInstance();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllMatchExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, condition);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAllMatchExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        array.accept(visitor);
        condition.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        array.forEachReference(action);
        condition.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("array", this.getArray().toJson());
        map.put("condition", this.getCondition().toJson());
        map.put("arrayType", this.getArrayType().toJson());
        map.put("type", this.getType().toJson());
        var cursorAlias = this.getCursorAlias();
        if (cursorAlias != null) map.put("cursorAlias", cursorAlias);
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_AllMatchExpression);
        super.write(output);
        array.write(output);
        condition.write(output);
    }
}
