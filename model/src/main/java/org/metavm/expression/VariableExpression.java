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
public class VariableExpression extends Expression {

    private final String variable;

    public VariableExpression(@NotNull String variable) {
        this.variable = variable;
    }

    @Generated
    public static VariableExpression read(MvInput input) {
        return new VariableExpression(input.readUTF());
    }

    @Generated
    public static void visit(StreamVisitor visitor) {
        visitor.visitUTF();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return variable;
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
        return List.of();
    }

    @Override
    protected Value evaluateSelf(EvaluationContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableExpression that)) return false;
        return Objects.equals(variable, that.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variable);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitVariableExpression(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Generated
    public void write(MvOutput output) {
        output.write(TYPE_VariableExpression);
        super.write(output);
        output.writeUTF(variable);
    }

    @Override
    public Expression transform(ExpressionTransformer transformer) {
        return new VariableExpression(variable);
    }
}
