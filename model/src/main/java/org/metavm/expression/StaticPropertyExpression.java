package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.FlowValue;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.util.ContextUtil;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class StaticPropertyExpression extends Expression {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final PropertyRef propertyRef;

    public StaticPropertyExpression(@NotNull PropertyRef propertyRef) {
        this.propertyRef = propertyRef;
    }

    @Generated
    public static StaticPropertyExpression read(MvInput input) {
        return new StaticPropertyExpression((PropertyRef) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        try(var serContext = SerializeContext.enter()) {
            var property = getProperty();
            return switch (symbolType) {
                case NAME -> property.getDeclaringType().getName() + "." + property.getName();
                case ID -> idVarName(serContext.getId(property.getDeclaringType())) + "." +
                        idVarName(serContext.getId(property));
            };
        }
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return propertyRef.getPropertyType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    public Property getProperty() {
        return propertyRef.getProperty();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        if(propertyRef instanceof FieldRef fieldRef) {
            var staticFieldTable = StaticFieldTable.getInstance(fieldRef.getDeclaringType(), ContextUtil.getEntityContext());
            return staticFieldTable.get(fieldRef.getRawField());
        }
        else if (propertyRef instanceof MethodRef method)
            return new FlowValue(method, null);
        else
            throw new IllegalStateException("Unknown property type: " + propertyRef);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaticPropertyExpression that)) return false;
        return Objects.equals(propertyRef, that.propertyRef);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propertyRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitStaticPropertyExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        propertyRef.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        propertyRef.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("property", this.getProperty());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_StaticPropertyExpression);
        super.write(output);
        output.writeValue(propertyRef);
    }
}
