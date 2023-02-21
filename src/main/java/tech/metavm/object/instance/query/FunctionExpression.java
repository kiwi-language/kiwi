package tech.metavm.object.instance.query;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;

@EntityType("函数表达式")
public class FunctionExpression extends Expression {
    @EntityField("函数")
    private final Function function;
    @ChildEntity("参数表达式列表")
    private final Table<Expression> arguments = new Table<>(Expression.class, true);

    public FunctionExpression(Function function) {
        this(function, new ArrayExpression(List.of()));
    }

    public FunctionExpression(Function function, Expression argument) {
        this.function = function;
        if(argument instanceof ArrayExpression arrayExpression) {
            arguments.addAll(arrayExpression.getExpressions());
        }
        else {
            arguments.add(argument);
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
    protected List<Expression> getChildren() {
        return Collections.unmodifiableList(arguments);
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        NncUtils.requireLength(children, this.arguments.size());
        return new FunctionExpression(function, new ArrayExpression(children));
    }

    @Override
    public <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.flatMap(arguments, arg -> arg.extractExpressions(klass));
    }

}
