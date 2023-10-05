package tech.metavm.expression;

import tech.metavm.util.NncUtils;

public class ExpressionTransformer {

    public Expression transform(Expression expression) {
        return switch (expression) {
            case BinaryExpression binaryExpression -> transformBinaryExpression(binaryExpression);
            case UnaryExpression unaryExpression -> transformUnaryExpression(unaryExpression);
            case FieldExpression fieldExpression -> transformFieldExpression(fieldExpression);
            case ArrayAccessExpression arrayAccessExpression -> transformArrayAccessExpression(arrayAccessExpression);
            case InstanceOfExpression instanceOfExpression -> transformInstanceOfExpression(instanceOfExpression);
            case ArrayExpression arrayExpression -> transformArrayExpression(arrayExpression);
            case FunctionExpression functionExpression -> transformFunctionExpression(functionExpression);
            case ThisExpression thisExpression -> transformThisExpression(thisExpression);
            case NodeExpression nodeExpression -> transformNodeExpression(nodeExpression);
            case StaticFieldExpression staticFieldExpression -> transformStaticFieldExpression(staticFieldExpression);
            case ConditionalExpression conditionalExpression -> transformConditionalExpression(conditionalExpression);
            case ConstantExpression constantExpression -> transformConstantExpression(constantExpression);
            case AllMatchExpression allMatchExpression -> transformAllMatchExpression(allMatchExpression);
            case CursorExpression cursorExpression -> transformCursorExpression(cursorExpression);
            case AsExpression asExpression -> transformAsExpression(asExpression);
            case VariableExpression variableExpression -> transformVariableExpression(variableExpression);
            case VariablePathExpression variablePathExpression -> transformVariablePathExpression(variablePathExpression);
            default -> throw new IllegalStateException("Unexpected value: " + expression);
        };
    }

    private Expression transformNodeExpression(NodeExpression nodeExpression) {
        return new NodeExpression(nodeExpression.getNode());
    }

    private Expression transformVariablePathExpression(VariablePathExpression variablePathExpression) {
        return new VariablePathExpression(
                transform(variablePathExpression.getQualifier()),
                variablePathExpression.getField()
        );
    }

    private Expression transformVariableExpression(VariableExpression variableExpression) {
        return new VariableExpression(variableExpression.getVariable());
    }

    private Expression transformAsExpression(AsExpression asExpression) {
        return new AsExpression(transform(asExpression.getExpression()), asExpression.getAlias());
    }

    private Expression transformCursorExpression(CursorExpression cursorExpression) {
        return new CursorExpression(
                transform(cursorExpression.getArray()),
                cursorExpression.getAlias()
        );
    }

    private Expression transformAllMatchExpression(AllMatchExpression allMatchExpression) {
        return new AllMatchExpression(
                transform(allMatchExpression.getArray()),
                transform(allMatchExpression.getCondition()),
                (CursorExpression) transform(allMatchExpression.getCursor())
        );
    }

    private Expression transformConstantExpression(ConstantExpression constantExpression) {
        return new ConstantExpression(constantExpression.getValue());
    }

    private Expression transformConditionalExpression(ConditionalExpression conditionalExpression) {
        return new ConditionalExpression(
                transform(conditionalExpression.getCondition()),
                transform(conditionalExpression.getTrueValue()),
                transform(conditionalExpression.getFalseValue())
        );
    }

    private Expression transformStaticFieldExpression(StaticFieldExpression staticFieldExpression) {
        return new StaticFieldExpression(staticFieldExpression.getField());
    }

    private Expression transformThisExpression(ThisExpression thisExpression) {
        return new ThisExpression(thisExpression.getType());
    }

    private Expression transformFunctionExpression(FunctionExpression functionExpression) {
        return new FunctionExpression(
                functionExpression.getFunction(),
                NncUtils.map(functionExpression.getArguments(), this::transform)
        );
    }

    private Expression transformArrayExpression(ArrayExpression arrayExpression) {
        return new ArrayExpression(NncUtils.map(arrayExpression.getExpressions(), this::transform));
    }

    private Expression transformInstanceOfExpression(InstanceOfExpression instanceOfExpression) {
        return new InstanceOfExpression(
                transform(instanceOfExpression.getOperand()),
                instanceOfExpression.getTargetType()
        );
    }

    private Expression transformArrayAccessExpression(ArrayAccessExpression arrayAccessExpression) {
        return new ArrayAccessExpression(
                transform(arrayAccessExpression.getArray()),
                transform(arrayAccessExpression.getIndex())
        );
    }

    private Expression transformFieldExpression(FieldExpression fieldExpression) {
        return new FieldExpression(
                transform(fieldExpression.getInstance()),
                fieldExpression.getField()
        );
    }

    private Expression transformUnaryExpression(UnaryExpression unaryExpression) {
        return new UnaryExpression(
                unaryExpression.getOperator(),
                transform(unaryExpression.getOperand())
        );
    }

    public Expression transformBinaryExpression(BinaryExpression binaryExpression) {
        return new BinaryExpression(
                binaryExpression.getOperator(),
                transform(binaryExpression.getFirst()),
                transform(binaryExpression.getSecond())
        );
    }

}
