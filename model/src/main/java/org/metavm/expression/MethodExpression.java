package org.metavm.expression;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.entity.ElementVisitor;
import org.metavm.flow.MethodRef;
import org.metavm.flow.ParameterRef;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.function.Consumer;

@Entity
public class MethodExpression extends Expression {

    @SuppressWarnings("unused")
    private static org.metavm.object.type.Klass __klass__;
    private final Expression self;

    private final MethodRef methodRef;

    public MethodExpression(@NotNull Expression self, @NotNull MethodRef methodRef) {
        this.self = self;
        this.methodRef = methodRef;
    }

    @Generated
    public static MethodExpression read(MvInput input) {
        return new MethodExpression(Expression.read(input), (MethodRef) input.readValue());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        visitor.visitValue();
    }

    public Expression getSelf() {
        return self;
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return self.buildSelf(symbolType, relaxedCheck) + "." + methodRef.getName()
                + "(" + Utils.join(methodRef.getParameters(), ParameterRef::getName, ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return methodRef.getPropertyType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(self);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return self.evaluate(context).resolveObject().getFunction(methodRef);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethodExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        self.accept(visitor);
        methodRef.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        self.forEachReference(action);
        methodRef.forEachReference(action);
    }

    public void buildJson(java.util.Map<String, Object> map) {
        map.put("self", this.getSelf().toJson());
        map.put("type", this.getType().toJson());
        map.put("components", this.getComponents().stream().map(Expression::toJson).toList());
        map.put("variableComponent", this.getVariableComponent().toJson());
        map.put("constantComponent", this.getConstantComponent().toJson());
        map.put("fieldComponent", this.getFieldComponent().toJson());
        map.put("arrayComponent", this.getArrayComponent().toJson());
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_MethodExpression);
        super.write(output);
        self.write(output);
        output.writeValue(methodRef);
    }
}
