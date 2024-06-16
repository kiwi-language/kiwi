package org.metavm.expression;

import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.ValueArray;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType
public class FunctionExpression extends Expression {
    private final Func function;
    private final ValueArray<Expression> arguments;

    public FunctionExpression(Func function, List<Expression> arguments) {
        this.function = function;
        this.arguments = new ValueArray<>(Expression.class, arguments);
    }

    public FunctionExpression(Func function, Expression argument) {
        this.function = function;
        List<Expression> expressions = argument instanceof ArrayExpression arrayExpression ?
                arrayExpression.getExpressions() : List.of(argument);
        arguments = new ValueArray<>(Expression.class, expressions);
    }

    public Func getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return arguments.toList();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return function.code() + "(" + NncUtils.join(arguments, arg -> arg.buildSelf(symbolType, relaxedCheck), ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return function.getReturnType(NncUtils.map(arguments, Expression::getType));
    }

    @Override
    public List<Expression> getChildren() {
        return NncUtils.listOf(arguments);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return function.evaluate(NncUtils.map(arguments, arg -> arg.evaluate(context)));
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.flatMap(arguments, arg -> arg.extractExpressions(klass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FunctionExpression that)) return false;
        return function == that.function && Objects.equals(arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(function, arguments);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitFunctionExpression(this);
    }
}
