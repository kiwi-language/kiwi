package tech.metavm.expression;

import tech.metavm.entity.CopyVisitor;
import tech.metavm.entity.Element;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.SerializeContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.InternalException;

import java.util.ArrayList;
import java.util.List;

@EntityType("表达式")
public abstract class Expression extends Element {

    protected abstract String buildSelf(VarType symbolType);

    public abstract int precedence();

    public final String build(VarType symbolType) {
        return build(symbolType, false);
    }

    protected final String build(VarType symbolType, boolean withParenthesis) {
        try (var serContext = SerializeContext.enter()) {
            if (serContext.isIncludeExpressionType())
                serContext.writeType(getType());
            String expr = buildSelf(symbolType);
            return withParenthesis ? "(" + expr + ")" : expr;
        }
    }

    public abstract Type getType();

    public abstract List<Expression> getChildren();

    public Expression getVariableChild() {
        for (Expression child : getChildren()) {
            if (child instanceof PropertyExpression || child instanceof ThisExpression) {
                return child;
            }
        }
        throw new InternalException("Can not find a variable child");
    }

    public <E extends Expression> E getChild(Class<E> type) {
        for (Expression child : getChildren()) {
            if (type.isInstance(child)) {
                return type.cast(child);
            }
        }
        throw new InternalException("Can not find a child expression of type '" + type.getName() + "'");
    }

    public ConstantExpression getConstChild() {
        return getChild(ConstantExpression.class);
    }

    public PropertyExpression getFieldChild() {
        return getChild(PropertyExpression.class);
    }

    public ArrayExpression getArrayChild() {
        return getChild(ArrayExpression.class);
    }

    @Override
    protected String toString0() {
        return getClass().getSimpleName();
    }

    public static String idVarName(Id id) {
        return "$" + id.toString();
    }

    public <T extends Expression> List<T> extractExpressions(Class<T> klass) {
        List<T> results = new ArrayList<>();
        if (klass.isInstance(this)) {
            results.add(klass.cast(this));
        }
        results.addAll(extractExpressionsRecursively(klass));
        return results;
    }

    public Instance evaluate(EvaluationContext context) {
        if (context.isContextExpression(this))
            return context.evaluate(this);
        else
            return evaluateSelf(context);
    }

    protected abstract Instance evaluateSelf(EvaluationContext context);

//    public Expression simplify() {
//        return substituteChildren(NncUtils.map(getChildren(), Expression::simplify));
//    }

    protected <T extends Expression> List<T> extractExpressionsRecursively(Class<T> klass) {
        return List.of();
    }

    public Expression copy() {
        return (Expression) accept(new CopyVisitor(this));
    }

}
