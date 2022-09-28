package tech.metavm.object.instance.query;

import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

public class FunctionExpression extends Expression {

    private final Function function;
    private final List<Expression> arguments;

    public FunctionExpression(Function function, Expression argument) {
        this.function = function;
        if(argument instanceof ListExpression listExpression) {
            arguments = listExpression.getExpressions();
        }
        else {
            arguments = List.of(argument);
        }
    }

    public Function getFunction() {
        return function;
    }

    public List<Expression> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return function + "(" + NncUtils.join(arguments, Objects::toString, ", ") + ")";
    }
}
