package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.Set;

public class SubEvaluationContext implements EvaluationContext {

    private final EvaluationContext parent;
    private final CursorExpression cursor;
    private final Instance instance;

    public SubEvaluationContext(EvaluationContext parent, CursorExpression cursor, Instance instance) {
        this.parent = parent;
        this.cursor = cursor;
        this.instance = instance;
    }

    @Override
    @Nullable
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(isSelfContextExpression(expression)) {
            return instance;
        }
        else if(parent != null && this.parent.isContextExpression(expression)) {
            return parent.evaluate(expression, evaluator);
        }
        else {
            throw new InternalException(expression + " is not a context expression of " + this);
        }
    }

    private boolean isSelfContextExpression(Expression expression) {
        return cursor == expression;
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return isSelfContextExpression(expression) || this.parent.isContextExpression(expression);
    }

}
