package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Generated;
import org.metavm.entity.StdKlass;
import org.metavm.expression.EvaluationContext;
import org.metavm.expression.Expression;
import org.metavm.expression.TypeLiteralExpression;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.function.Consumer;

public class TypeValue extends Value {

    private final Type type;

    public TypeValue(Type type) {
        this.type = type;
    }

    @Generated
    public static TypeValue read(MvInput input) {
        return new TypeValue(input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    public org.metavm.object.instance.core.@NotNull Value evaluate(EvaluationContext context) {
        var klass = Types.getKlass(type);
        return klass.getReference();
    }

    @Override
    public Type getType() {
        return StdKlass.type.get().getType();
    }

    @Override
    public String getText() {
        return type.getTypeDesc();
    }

    @Override
    public Expression getExpression() {
        return new TypeLiteralExpression(type);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("text", this.getText());
        map.put("expression", this.getExpression().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_TypeValue);
        super.write(output);
        output.writeValue(type);
    }
}
