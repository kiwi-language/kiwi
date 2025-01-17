package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Entity
public class ThisExpression extends Expression {

    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
    private final ClassType type;

    public ThisExpression(ClassType type) {
        this.type = type;
    }

    @Generated
    public static ThisExpression read(MvInput input) {
        return new ThisExpression((ClassType) input.readType());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitValue();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "this";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public ClassType getType() {
        return type;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return context.evaluate(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThisExpression that)) return false;
        return Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitThisExpression(this);
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
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ThisExpression);
        super.write(output);
        output.writeValue(type);
    }
}
