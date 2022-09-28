package tech.metavm.object.instance.query;

import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtil;

import java.util.List;
import java.util.Objects;

import static tech.metavm.object.instance.query.ExpressionUtil.*;

public class ExpressionEvaluater {

    public static Object evaluate(ObjectTree tree, Expression expression) {
        return new ExpressionEvaluater(tree, expression).evaluate();
    }

    private final ObjectTree objectTree;
    private final Expression expression;

    private ExpressionEvaluater(ObjectTree objectTree, Expression expression) {
        this.objectTree = objectTree;
        this.expression = expression;
    }

    private Object evaluate() {
        return evaluate(expression);
    }

    private Object evaluate(Expression expression) {
        if(expression instanceof UnaryExpression unaryExpression) {
            return evaluateUnary(unaryExpression);
        }
        if(expression instanceof BinaryExpression binaryExpression) {
            return evaluateBinary(binaryExpression);
        }
        if(expression instanceof ConstantExpression constantExpression) {
            return evaluateConst(constantExpression);
        }
        if(expression instanceof FieldExpression fieldExpression) {
            return evaluateField(fieldExpression);
        }
        if(expression instanceof ListExpression listExpression) {
            return evaluateList(listExpression);
        }
        if(expression instanceof FunctionExpression functionExpression) {
            return evaluateFunction(functionExpression);
        }
        throw new RuntimeException("Unsupported expression: " + expression);
    }

    private Object evaluateFunction(FunctionExpression functionExpression) {
        List<Object> argValues = NncUtils.map(functionExpression.getArguments(), this::evaluate);
        return functionExpression.getFunction().evaluate(argValues);
    }

    private Object evaluateList(ListExpression listExpression) {
        return NncUtils.map(
                listExpression.getExpressions(),
                this::evaluate
        );
    }

    private Object evaluateField(FieldExpression fieldExpression) {
        return objectTree.getFieldValue(fieldExpression.getFieldPath());
    }

    private Object evaluateConst(ConstantExpression constantExpression) {
        return constantExpression.getValue();
    }

    private Object evaluateBinary(BinaryExpression binaryExpression) {
        Operator op = binaryExpression.getOperator();
        Object firstValue = evaluate(binaryExpression.getFirst()),
                secondValue = evaluate(binaryExpression.getSecond());
        if(op == Operator.EQ) {
            return Objects.equals(firstValue, secondValue);
        }
        if(op == Operator.NE) {
            return !Objects.equals(firstValue, secondValue);
        }
        if(op == Operator.GE) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) >= castInteger(secondValue);
            }
            return castFloat(firstValue) >= castFloat(secondValue);
        }
        if(op == Operator.GT) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) > castInteger(secondValue);
            }
            return castFloat(firstValue) > castFloat(secondValue);
        }
        if(op == Operator.LE) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) <= castInteger(secondValue);
            }
            return castFloat(firstValue) <= castFloat(secondValue);
        }
        if(op == Operator.LT) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) < castInteger(secondValue);
            }
            return castFloat(firstValue) < castFloat(secondValue);
        }
        if(op == Operator.ADD) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) + castInteger(secondValue);
            }
            return castFloat(firstValue) + castFloat(secondValue);
        }
        if(op == Operator.SUBTRACT) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) - castInteger(secondValue);
            }
            return castFloat(firstValue) - castFloat(secondValue);
        }
        if(op == Operator.MULTIPLY) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) * castInteger(secondValue);
            }
            return castFloat(firstValue) * castFloat(secondValue);
        }
        if(op == Operator.DIVIDE) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) / castInteger(secondValue);
            }
            return castFloat(firstValue) / castFloat(secondValue);
        }
        if(op == Operator.MOD) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue) % castInteger(secondValue);
            }
            return castFloat(firstValue) % castFloat(secondValue);
        }
        if(op == Operator.STARTS_WITH) {
            return castString(firstValue).startsWith(castString(secondValue));
        }
        if(op == Operator.LIKE) {
            return castString(firstValue).contains(castString(secondValue));
        }
        if(op == Operator.IN) {
            return castCollection(firstValue).contains(secondValue);
        }
        if(op == Operator.AND) {
            return castBoolean(firstValue) && castBoolean(secondValue);
        }
        if(op == Operator.OR) {
            return castBoolean(firstValue) || castBoolean(secondValue);
        }
        throw new RuntimeException("Unsupported operator " + op + " for binary expressions");
    }

    private Object evaluateUnary(UnaryExpression expr) {
        Operator op = expr.getOperator();
        Object value = evaluate(expr.getOperand());
        if(op == Operator.NOT) {
            return !castBoolean(value);
        }
        if(op == Operator.NEGATE) {
            if(ValueUtil.isInteger(value)) {
                return -castInteger(value);
            }
            return -castFloat(value);
        }
        if(op == Operator.IS_NOT_NULL) {
            return value != null;
        }
        if(op == Operator.IS_NULL) {
            return value == null;
        }
        throw BusinessException.invalidExpression("不支持的运算符: " + expr.getOperator());
    }

}
