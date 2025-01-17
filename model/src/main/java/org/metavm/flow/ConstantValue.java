package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.expression.ConstantExpression;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

@Entity
public class ConstantValue extends Value {

    private final org.metavm.object.instance.core.Value value;

    public ConstantValue(org.metavm.object.instance.core.Value value) {
        this.value = value;
    }

    @Generated
    public static ConstantValue read(MvInput input) {
        return new ConstantValue(input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }


    @Override
    public Type getType() {
        return value.getValueType();
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        return value;
    }

    @Override
    public String getText() {
        return value.getText();
    }

    @Override
    public Expression getExpression() {
        return new ConstantExpression(value);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (value instanceof Reference r) action.accept(r);
        else if (value instanceof org.metavm.object.instance.core.NativeValue t) t.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("text", this.getText());
        map.put("expression", this.getExpression().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ConstantValue);
        super.write(output);
        output.writeValue(value);
    }
}
