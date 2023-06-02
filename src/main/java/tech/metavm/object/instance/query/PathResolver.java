package tech.metavm.object.instance.query;

import tech.metavm.expression.*;
import tech.metavm.util.IdentitySet;

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
        if(visited.contains(expression)) {
            throw new RuntimeException("Circular reference in expression");
        }
        visited.add(expression);
        if(expression instanceof UnaryExpression unaryExpression) {
            fillPath0(unaryExpression.getOperand(), visited);
        }
        else if(expression instanceof BinaryExpression binaryExpression) {
            fillPath0(binaryExpression.getFirst(), visited);
            fillPath0(binaryExpression.getSecond(), visited);
        }
        else if(expression instanceof ArrayExpression listExpression) {
            for (Expression expr : listExpression.getExpressions()) {
                fillPath0(expr, visited);
            }
        }
        else if(expression instanceof FieldExpression fieldExpression) {
            root.fillPath(fieldExpression.getPathString());
        }
    }

}
