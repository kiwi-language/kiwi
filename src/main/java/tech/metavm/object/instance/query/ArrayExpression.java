package tech.metavm.object.instance.query;

import tech.metavm.entity.ValueType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.ValueUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ValueType("数组表达式")
public class ArrayExpression extends Expression {

    private final Table<Expression> expressions;

    public ArrayExpression(List<Expression> expressions) {
        this.expressions = new Table<>(expressions);
    }

    public static ArrayExpression merge(Expression first, Expression second) {
        if(first instanceof ArrayExpression listExpression) {
            List<Expression> rest = listExpression.expressions;
            List<Expression> expressions = new ArrayList<>(rest.size() + 1);
            expressions.addAll(rest);
            expressions.add(second);
            return new ArrayExpression(expressions/*, first.context*/);
        }
        else {
            return new ArrayExpression(List.of(first, second)/*, first.context*/);
        }
    }

    public Table<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return NncUtils.join(expressions, expr -> expr.buildSelf(symbolType), ", ");
    }

    @Override
    public int precedence() {
        return Operator.COMMA.precedence();
    }

    @Override
    public Type getType() {
        return ValueUtil.getCommonSuperType(NncUtils.map(expressions, Expression::getType)).getArrayType();
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.flatMap(expressions, expr -> expr.extractExpressions(klass));
    }

}
