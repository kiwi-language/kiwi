package org.metavm.expression;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.AnyType;
import org.metavm.object.type.Type;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Getter
@Wire
@Entity
public class VariablePathExpression extends Expression {

    private final Expression qualifier;
    private final VariableExpression field;

    public VariablePathExpression(@NotNull Expression qualifier, @NotNull VariableExpression field) {
        this.qualifier = qualifier;
        this.field = field;
    }

    @Generated
    public static VariablePathExpression read(MvInput input) {
        return new VariablePathExpression(Expression.read(input), VariableExpression.read(input));
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        VariableExpression.visit(visitor);
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return qualifier.build(symbolType, false, relaxedCheck) + "." + field.buildSelf(symbolType, relaxedCheck);
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return AnyType.instance;
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(qualifier, field);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariablePathExpression that)) return false;
        return Objects.equals(qualifier, that.qualifier) && Objects.equals(field, that.field);
    }

    @Override
    public int hashCode() {
        return Objects.hash(qualifier, field);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariablePathExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        qualifier.accept(visitor);
        field.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        qualifier.forEachReference(action);
        field.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_VariablePathExpression);
        super.write(output);
        qualifier.write(output);
        field.write(output);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new VariablePathExpression(qualifier.accept(transformer), field);
    }
}
