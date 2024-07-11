package org.metavm.expression;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ValueArray;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.ArrayKind;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Types;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@EntityType
public class ArrayExpression extends Expression {

    public static ArrayExpression create(List<Expression> expressions) {
        var type = new ArrayType(Types.getLeastUpperBound(NncUtils.map(expressions, Expression::getType)), ArrayKind.READ_ONLY);
        return new ArrayExpression(expressions, type);
    }

    private final ValueArray<Expression> expressions;
    private final ArrayType type;

    public ArrayExpression(Collection<Expression> expressions, ArrayType type) {
        this.expressions = new ValueArray<>(Expression.class, expressions);
        this.type = type;
    }

    public static ArrayExpression merge(Expression first, Expression second) {
        if (first instanceof ArrayExpression listExpression) {
            var rest = listExpression.expressions;
            List<Expression> expressions = new ArrayList<>(rest.size() + 1);
            expressions.addAll(rest.toList());
            expressions.add(second);
            return create(expressions);
        } else {
            return create(List.of(first, second));
        }
    }

    public List<Expression> getExpressions() {
        return expressions.toList();
    }

    @Override
    public String buildSelf(VarType symbolType, boolean relaxedCheck) {
        return "[" + NncUtils.join(expressions, expr -> expr.buildSelf(symbolType, relaxedCheck), ", ") + "]";
    }

    @Override
    public int precedence() {
        return 9;
    }

    @Override
    public ArrayType getType() {
        return type;
    }

    @Override
    public List<Expression> getChildren() {
        return NncUtils.listOf(expressions);
    }

    @Override
    protected Instance evaluateSelf(EvaluationContext context) {
        return new ArrayInstance(type, NncUtils.map(expressions, e -> e.evaluate(context))).getReference();
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

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitArrayExpression(this);
    }
}
