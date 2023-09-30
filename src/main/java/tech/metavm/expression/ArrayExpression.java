package tech.metavm.expression;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.TypeUtil;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;
import tech.metavm.util.ValueUtil;

import java.util.*;

@EntityType("数组表达式")
public class ArrayExpression extends Expression {

    private final Table<Expression> expressions = new Table<>(Expression.class, true);

    public ArrayExpression(Collection<Expression> expressions) {
        this.expressions.addAll(expressions);
    }

    public static ArrayExpression merge(Expression first, Expression second) {
        if (first instanceof ArrayExpression listExpression) {
            List<Expression> rest = listExpression.expressions;
            List<Expression> expressions = new ArrayList<>(rest.size() + 1);
            expressions.addAll(rest);
            expressions.add(second);
            return new ArrayExpression(expressions/*, first.context*/);
        } else {
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
        return TypeUtil.getArrayType(
                ValueUtil.getCommonSuperType(NncUtils.map(expressions, Expression::getType))
        );
    }

    @Override
    protected List<Expression> getChildren() {
        return Collections.unmodifiableList(expressions);
    }

    @Override
    public Expression cloneWithNewChildren(List<Expression> children) {
        return new ArrayExpression(children);
    }

    @Override
    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return NncUtils.flatMap(expressions, expr -> expr.extractExpressions(klass));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ArrayExpression that)) return false;
        return Objects.equals(expressions, that.expressions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expressions);
    }
}
