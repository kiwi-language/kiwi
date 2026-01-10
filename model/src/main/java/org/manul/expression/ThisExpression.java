package org.manul.expression;

import org.manul.api.Entity;
import org.manul.api.Generated;
import org.manul.wire.Wire;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.Value;
import org.manul.object.type.ClassType;
import org.manul.util.MvInput;
import org.manul.util.MvOutput;
import org.manul.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire
@Entity
public class ThisExpression extends Expression {

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

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_ThisExpression);
        super.write(output);
        output.writeValue(type);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new ThisExpression(type);
    }
}
