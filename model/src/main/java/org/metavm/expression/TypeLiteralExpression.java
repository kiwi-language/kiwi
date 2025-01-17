package org.metavm.expression;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class TypeLiteralExpression extends Expression {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final Type type;

    public TypeLiteralExpression(Type type) {
        this.type = type;
    }

    @Generated
    public static TypeLiteralExpression read(MvInput input) {
        return new TypeLiteralExpression(input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            return type.toExpression(serContext) + ".class";
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return StdKlass.type.get().getType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        var klass = Types.getKlass(type);
        return klass.getReference();
    }

    public Type getTypeObject() {
        return type;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTypeLiteralExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        type.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        type.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("typeObject", this.getTypeObject().toJson());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_TypeLiteralExpression);
        super.write(output);
        output.writeValue(type);
    }
}
