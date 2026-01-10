package org.manul.flow;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.expression.ArrayExpression;
import org.manul.expression.EvaluationContext;
import org.manul.expression.Expression;
import org.manul.object.instance.core.ArrayInstance;
import org.manul.object.instance.core.Reference;
import org.manul.object.type.ArrayType;
import org.manul.object.type.Type;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.Utils;
import org.manul.util.StreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Wire
public class ArrayValue extends Value {

    private final List<Value> elements;
    private final ArrayType type;

    public ArrayValue(List<Value> elements, ArrayType type) {
        this.elements = new ArrayList<>(elements);
        this.type = type;
    }

    @Generated
    public static ArrayValue read(MvInput input) {
        return new ArrayValue(input.readList(() -> Value.read(input)), (ArrayType) input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitList(() -> Value.visit(visitor));
        visitor.visitValue();
    }

    @Override
    public org.manul.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return new ArrayInstance(type, Utils.map(elements, e -> e.evaluate(context))).getReference();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String getText() {
        return "[" + Utils.join(elements, Value::getText) + "]";
    }

    @Override
    public Expression getExpression() {
        return new ArrayExpression(Utils.map(elements, Value::getExpression), type);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        for (var elements_ : elements) elements_.forEachReference(action);
        type.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ArrayValue);
        super.write(output);
        output.writeList(elements, arg0 -> arg0.write(output));
        output.writeValue(type);
    }
}
