package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

@Entity
public class ArrayAccessExpression extends Expression {

    private final Expression array;
    private final Expression index;

    public ArrayAccessExpression(@NotNull Expression array, @NotNull Expression index) {
//        NncUtils.requireTrue(array.getType() instanceof ArrayType);
        this.array = array;
        this.index = index;
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
    public List<Expression> getChildren() {
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
}
