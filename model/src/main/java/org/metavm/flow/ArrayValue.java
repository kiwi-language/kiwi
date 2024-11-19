package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ValueArray;
import org.metavm.expression.ArrayExpression;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.List;

public class ArrayValue extends Value {

    private final ValueArray<Value> elements;
    private final ArrayType type;

    public ArrayValue(List<Value> elements, ArrayType type) {
        this.elements = new ValueArray<>(Value.class, elements);
        this.type = type;
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return new ArrayInstance(type, NncUtils.map(elements, e -> e.evaluate(context))).getReference();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getText() {
        return "[" + NncUtils.join(elements, Value::getText) + "]";
    }

    @Override
    public Expression getExpression() {
        return new ArrayExpression(NncUtils.map(elements, Value::getExpression), type);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayValue(this);
    }
}
