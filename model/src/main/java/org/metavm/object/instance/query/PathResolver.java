package org.metavm.object.instance.query;

import org.metavm.expression.*;
import org.metavm.util.IdentitySet;
import org.metavm.util.LinkedList;

import java.util.List;

public class PathResolver {

    public static PathTree resolvePath(List<Expression> expressions) {
        PathResolver resolver = new PathResolver();
        expressions.forEach(resolver::fillPath);
        return resolver.root;
    }

    private final PathTree root = new PathTree("root");

    private void fillPath(Expression expression) {
        fillPath0(expression, new IdentitySet<>());
    }

    private void fillPath0(Expression expression, IdentitySet<Expression> visited) {
        if(visited.contains(expression))
            throw new RuntimeException("Circular reference in expression");
        visited.add(expression);
        if(expression instanceof UnaryExpression unaryExpression)
            fillPath0(unaryExpression.getOperand(), visited);
        else if(expression instanceof BinaryExpression binaryExpression) {
            fillPath0(binaryExpression.getLeft(), visited);
            fillPath0(binaryExpression.getRight(), visited);
        }
        else if(expression instanceof ArrayExpression listExpression) {
            for (Expression expr : listExpression.getExpressions()) {
                fillPath0(expr, visited);
            }
        }
        else if(expression instanceof PropertyExpression fieldExpression)
            root.fillPath(getFieldPath(fieldExpression).toString());
    }

    public static Path getFieldPath(PropertyExpression expression) {
        var stack = new LinkedList<String>();
        Expression expr = expression;
        while (expr instanceof PropertyExpression propExpr) {
            stack.push(propExpr.getProperty().getName());
            expr = propExpr.getInstance();
        }
        if(expr instanceof ThisExpression)
            return new Path(stack, 0, stack.size());
        else
            throw new RuntimeException("Unsupported expression");
    }

}
