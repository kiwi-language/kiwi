package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;

import java.util.Set;

public class InstanceEvaluationContext implements EvaluationContext {

    private final ClassInstance instance;

    public InstanceEvaluationContext(ClassInstance instance) {
        this.instance = instance;
    }

    @Override
    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof FieldExpression fieldExpr) {
            return instance.getResolved(fieldExpr.getFieldIds());
        }
        if(expression instanceof ThisExpression thisExpression) {
            return instance;
        }
        else {
            throw new RuntimeException("Context " + this.getClass().getName() + " doesn't support expression: "
                    + expression.getClass().getName());
        }
    }

    @Override
    public Set<Class<? extends Expression>> supportedExpressionClasses() {
        return Set.of(ThisExpression.class, FieldExpression.class);
    }

}
