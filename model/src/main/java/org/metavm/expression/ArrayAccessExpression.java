package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class ArrayAccessExpression extends Expression {

    private final Expression array;
    private final Expression index;

    public ArrayAccessExpression(@NotNull Expression array, @NotNull Expression index) {
//        NncUtils.requireTrue(array.getType() instanceof ArrayType);
        this.array = array;
        this.index = index;
    }

    @Generated
    public static ArrayAccessExpression read(MvInput input) {
        return new ArrayAccessExpression(Expression.read(input), Expression.read(input));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        Expression.visit(visitor);
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return array.build(symbolType, false, relaxedCheck)
                + "[" + index.build(symbolType, false, relaxedCheck) + "]";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return ((ArrayType) array.getType()).getElementType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(array, index);
    }

    public Expression getArray() {
        return array;
    }

    public Expression getIndex() {
        return index;
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        int i = ((LongValue) (index.evaluate(context))).getValue().intValue();
        return array.evaluate(context).resolveArray().get(i);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayAccessExpression that)) return false;
        return Objects.equals(array, that.array) && Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(array, index);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayAccessExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        array.accept(visitor);
        index.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        array.forEachReference(action);
        index.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("array", this.getArray().toJson());
        map.put("index", this.getIndex().toJson());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ArrayAccessExpression);
        super.write(output);
        array.write(output);
        index.write(output);
    }
}
