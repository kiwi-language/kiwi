package tech.metavm.object.instance.query;

import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.List;

public class FunctionExpression extends Expression {

    private final Function function;
    private final List<Expression> arguments;

    public FunctionExpression(Function function, Expression argument/*, InstanceContext context*/) {
//        super(context);
        this.function = function;
        if(argument instanceof ArrayExpression arrayExpression) {
            arguments = arrayExpression.getExpressions();
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
    public String buildSelf(VarType symbolType) {
        return function + "(" + NncUtils.join(arguments, arg -> arg.buildSelf(symbolType), ", ") + ")";
    }

    @Override
    public int precedence() {
        return 0;
    }

    @Override
    public Type getType() {
        return function.getResultType(NncUtils.map(arguments, Expression::getType)/*, context*/);
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.flatMap(arguments, arg -> arg.extractExpressions(klass));
    }

}
