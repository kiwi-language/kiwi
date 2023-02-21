package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.InternalException;

import java.util.Set;

public class InstanceEvaluationContext implements EvaluationContext {

    private final ClassInstance instance;

    public InstanceEvaluationContext(ClassInstance instance) {
        this.instance = instance;
    }

    @Override
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(isContextExpression(expression)) {
            return instance;
        }
        else {
            throw new InternalException(evaluator + " is not a context expression of " + this);
        }
    }

    @Override
    public boolean isContextExpression(Expression expression) {
        return expression instanceof ThisExpression;
    }

}
