package tech.metavm.flow;

import tech.metavm.entity.InstanceContext;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.query.*;
import tech.metavm.util.InternalException;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FlowLoadingBuffer implements EvaluationContext {

    private final Map<NodeRT<?>, Path> node2path = new HashMap<>();
    private final Map<NodeRT<?>, Instance> node2instance = new HashMap<>();
    private final Map<NodeRT<?>, Expression> node2expression = new HashMap<>();
    private final InstanceContext context;

    public FlowLoadingBuffer(InstanceContext context) {
        this.context = context;
    }

    public void addExpression(FieldExpression expression) {
        expression = expression.simply();
        if(expression.getInstance() instanceof NodeExpression nodeExpression) {
            getNodePath(nodeExpression.getNode()).addPath(expression.getFieldIdPath());
        }
    }

    private Path getNodePath(NodeRT<?> node) {
        return node2path.computeIfAbsent(node, k -> Path.createRoot());
    }

    public Instance evaluate(Expression expression, ExpressionEvaluator evaluator) {
        if(expression instanceof NodeExpression nodeExpression) {
            return evaluateNodeExpr(nodeExpression, evaluator);
        }
        throw new InternalException("Unsupported expression: " + expression.getClass().getName());
    }

    @Override
    public Set<Class<? extends Expression>> supportedExpressionClasses() {
        return Set.of(NodeExpression.class);
    }

    public Instance evaluateNodeExpr(NodeExpression expression, ExpressionEvaluator evaluator) {
        NodeRT<?> node = expression.getNode();
        Instance instance;
        if((instance = node2instance.get(node)) != null) {
            return instance;
        }
        instance = (Instance) evaluator.evaluate(node2expression.get(node));
        node2instance.put(node, instance);
        return instance;
    }


//        expression = expand(expression);
//        List<FieldExpression> fieldExpressions = expression.extractExpressions(FieldExpression.class);
        /*
        Case one:
        Instance is also a field expression. We should simplify it.
        In that way, the category of instance expression is limited.
        This works but restricts the generality of the architecture.

        What if we make no assumption of the instance expression?



         */

}
