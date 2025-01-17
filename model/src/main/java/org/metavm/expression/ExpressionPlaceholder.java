package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.NeverType;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.function.Consumer;

@Entity
public class ExpressionPlaceholder extends Expression {
    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;

    @Generated
    public static ExpressionPlaceholder read(MvInput input) {
        return new ExpressionPlaceholder();
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return NeverType.instance;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitExpressionPlaceholder(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
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
        output.write(TYPE_ExpressionPlaceholder);
        super.write(output);
    }
}
