package org.metavm.expression;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.api.Generated;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
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
public class AsExpression extends Expression {

    private final Expression expression;
    private final String alias;

    public AsExpression(@NotNull Expression expression, @NotNull String alias) {
        this.expression = expression;
        this.alias = alias;
    }

    @Generated
    public static AsExpression read(MvInput input) {
        return new AsExpression(Expression.read(input), input.readUTF());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        Expression.visit(visitor);
        visitor.visitUTF();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return expression.buildSelf(symbolType, relaxedCheck) + " as " + alias;
    }

    @Override
    public int precedence() {
        return 100;
    }

    @Override
    public Type getType() {
        return expression.getType();
    }

    @Override
    public List<Expression> getComponents() {
        return List.of(expression);
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        return expression.evaluate(context);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AsExpression that)) return false;
        return Objects.equals(expression, that.expression) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, alias);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitAsExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        expression.accept(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        expression.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_AsExpression);
        super.write(output);
        expression.write(output);
        output.writeUTF(alias);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new AsExpression(expression.accept(transformer), alias);
    }
}
