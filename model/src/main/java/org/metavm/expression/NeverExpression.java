package org.metavm.expression;

import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.InstanceVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.function.Consumer;

public class NeverExpression extends Expression {
    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;

    @Generated
    public static NeverExpression read(MvInput input) {
        return new NeverExpression();
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
    }

    @Override
    protected String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "never";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return Types.getNeverType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new IllegalStateException("NeverExpression should not be evaluated");
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitNeverExpression(this);
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
        output.write(TYPE_NeverExpression);
        super.write(output);
    }
}
