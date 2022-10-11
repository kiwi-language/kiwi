package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;

import java.util.Set;

public class InstanceEvaluationContext implements EvaluationContext {

    private final Instance instance;

    public InstanceEvaluationContext(Instance instance) {
        this.instance = instance;
    }

    @Override
    public Object evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof FieldExpression fieldExpr) {
            return instance.getResolved(fieldExpr.getFieldIds());
        }
        else {
            throw new RuntimeException("Context " + this.getClass().getName() + " doesn't support expression: "
                    + expression.getClass().getName());
        }
    }

    @Override
    public Set<Class<? extends Expression>> supportedExpressionClasses() {
        return Set.of(FieldExpression.class);
    }

}
