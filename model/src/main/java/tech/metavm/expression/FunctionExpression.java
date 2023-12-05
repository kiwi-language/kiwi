package tech.metavm.expression;

import tech.metavm.entity.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

@EntityType("函数表达式")
public class FunctionExpression extends Expression {
    @EntityField("函数")
    private final Function function;
    @ChildEntity("参数表达式列表")
    private final ChildArray<Expression> arguments = addChild(new ChildArray<>(Expression.class), "arguments");

    public FunctionExpression(Function function, List<Expression> arguments) {
        this.function = function;
        this.arguments.addChildren(NncUtils.map(arguments, Expression::copy));
    }


    public FunctionExpression(Function function, Expression argument) {
        this.function = function;
        if(argument instanceof ArrayExpression arrayExpression) {
            arguments.addChildren(arrayExpression.getExpressions());
        }
        else {
            arguments.addChild(argument);
        }
    }

    public Function getFunction() {
        return function;
    }

    public ChildArray<Expression> getArguments() {
        return arguments;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return function.code() + "(" + NncUtils.join(arguments, arg -> arg.buildSelf(symbolType), ", ") + ")";
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
    public Expression substituteChildren(List<Expression> children) {
        NncUtils.requireLength(children, this.arguments.size());
        return new FunctionExpression(function, children);
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
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
