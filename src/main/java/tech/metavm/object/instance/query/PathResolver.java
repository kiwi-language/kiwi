package tech.metavm.object.instance.query;

import tech.metavm.util.IdentitySet;

import java.util.List;

public class PathResolver {

    public static Path resolvePath(List<Expression> expressions) {
        PathResolver resolver = new PathResolver();
        expressions.forEach(resolver::fillPath);
        return resolver.root;
    }

    private final Path root = new Path("root");

    private void fillPath(Expression expression) {
        fillPath0(expression, new IdentitySet<>());
    }

    private void fillPath0(Expression expression, IdentitySet<Expression> visited) {
        if(visited.contains(expression)) {
            throw new RuntimeException("Circular reference in expressionw");
        }
        visited.add(expression);
        if(expression instanceof UnaryExpression unaryExpression) {
            fillPath0(unaryExpression.getOperand(), visited);
        }
        else if(expression instanceof BinaryExpression binaryExpression) {
            fillPath0(binaryExpression.getFirst(), visited);
            fillPath0(binaryExpression.getSecond(), visited);
        }
        else if(expression instanceof ListExpression listExpression) {
            for (Expression expr : listExpression.getExpressions()) {
                fillPath0(expr, visited);
            }
        }
        else if(expression instanceof FieldExpression fieldExpression) {
            root.fillPath(fieldExpression.getFieldPath());
        }
    }

}
