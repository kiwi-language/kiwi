package tech.metavm.expression;

import tech.metavm.entity.*;
import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Types;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@EntityType("数组表达式")
public class ArrayExpression extends Expression {

    public static ArrayExpression create(List<Expression> expressions, IEntityContext entityContext) {
        var type = entityContext.getArrayType(Types.getLeastUpperBound(NncUtils.map(expressions, Expression::getType)), ArrayKind.READ_ONLY);
        return new ArrayExpression(expressions, type);
    }

    @ChildEntity("表达式列表")
    private final ChildArray<Expression> expressions = addChild(new ChildArray<>(Expression.class), "expressions");
    @EntityField("类型")
    private final ArrayType type;

    public ArrayExpression(Collection<Expression> expressions, ArrayType type) {
        this.expressions.addChildren(NncUtils.map(expressions, Expression::copy));
        this.type = type;
    }

    public static ArrayExpression merge(Expression first, Expression second, IEntityContext entityContext) {
        if (first instanceof ArrayExpression listExpression) {
            var rest = listExpression.expressions;
            List<Expression> expressions = new ArrayList<>(rest.size() + 1);
            rest.forEach(expressions::add);
            expressions.add(second);
            return create(expressions, entityContext);
        } else {
            return create(List.of(first, second), entityContext);
        }
    }

    public ChildArray<Expression> getExpressions() {
        return expressions;
    }

    @Override
    public String buildSelf(VarType symbolType) {
        return "[" + NncUtils.join(expressions, expr -> expr.buildSelf(symbolType), ", ") + "]";
    }

    @Override
    public int precedence() {
        return 9;
    }

    @Override
    public ArrayType getType() {
        return type;
//        return TypeUtil.getArrayType(
//                ValueUtil.getCommonSuperType(NncUtils.map(expressions, Expression::getType))
//        );
    }

    @Override
    public List<Expression> getChildren() {
        return NncUtils.listOf(expressions);
    }

    @Override
    public Instance evaluate(EvaluationContext context) {
        return new ArrayInstance(type, NncUtils.map(expressions, e -> e.evaluate(context)));
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
