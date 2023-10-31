package tech.metavm.expression;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.List;

@EntityType("表达式")
public abstract class Expression extends Entity {

    public abstract String buildSelf(VarType symbolType);

    public abstract int precedence();

    public final String build(VarType symbolType, boolean withParenthesis) {
        String expr = buildSelf(symbolType);
        return withParenthesis ? "(" + expr + ")" : expr;
    }

    public abstract Type getType();

    protected abstract List<Expression> getChildren();

    public Expression getVariableChild() {
        for (Expression child : getChildren()) {
            if(child instanceof PropertyExpression || child instanceof ThisExpression) {
                return child;
            }
        }
        throw new InternalException("Can not find a variable child");
    }

    public <E extends Expression> E getChild(Class<E> type) {
        for (Expression child : getChildren()) {
            if(type.isInstance(child)) {
                return type.cast(child);
            }
        }
        throw new InternalException("Can not find a child expression of type '" + type.getName() + "'");
    }

    public ConstantExpression getConstChild() {
        return getChild(ConstantExpression.class);
    }

    public abstract Expression cloneWithNewChildren(List<Expression> children);

    public PropertyExpression getFieldChild() {
        return getChild(PropertyExpression.class);
    }

    public ArrayExpression getArrayChild() {
        return getChild(ArrayExpression.class);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + buildSelf(VarType.NAME);
    }

    public static String idVarName(long id) {
        return "$" + id;
    }

    public <T extends Expression> List<T> extractExpressions(Class<T> klass) {
        List<T> results = new ArrayList<>();
        if(klass.isInstance(this)) {
            results.add(klass.cast(this));
        }
        results.addAll(extractExpressionsRecursively(klass));
        return results;
    }

    protected  <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return List.of();
    }

}
