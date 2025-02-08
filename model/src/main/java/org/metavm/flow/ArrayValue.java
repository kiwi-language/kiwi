package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.expression.ArrayExpression;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
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

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("text", this.getText());
        map.put("expression", this.getExpression().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ArrayValue);
        super.write(output);
        output.writeList(elements, arg0 -> arg0.write(output));
        output.writeValue(type);
    }
}
