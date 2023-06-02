package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRExpression;
import tech.metavm.transpile.ir.TypeVariable;

import java.util.Objects;

public class ExprXType extends XType {

    private final IRExpression expression;
    private final TypeVariable<?> typeVariable;

    public ExprXType(IRExpression expression, TypeVariable<?> typeVariable) {
        super(typeVariable.getName());
        this.typeVariable = typeVariable;
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExprXType exprXType)) return false;
        return Objects.equals(expression, exprXType.expression) && Objects.equals(typeVariable, exprXType.typeVariable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, typeVariable);
    }
}
