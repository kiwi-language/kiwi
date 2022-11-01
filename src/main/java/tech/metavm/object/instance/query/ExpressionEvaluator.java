package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.EnumConstant;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

import static tech.metavm.object.instance.query.ExpressionUtil.*;

public class ExpressionEvaluator {

    public static Object evaluate(Expression expression, ObjectTree objectTree) {
        return new ExpressionEvaluator(expression, new TreeEvaluationContext(objectTree)).evaluate();
    }

    public static Object evaluate(Expression expression, Instance instance) {
        return new ExpressionEvaluator(expression, new InstanceEvaluationContext(instance)).evaluate();
    }

    public static Object evaluate(Expression expression, EvaluationContext context) {
        return new ExpressionEvaluator(expression, context).evaluate();
    }

    private final EvaluationContext context;
    private final Expression expression;

    private ExpressionEvaluator(Expression expression, EvaluationContext context) {
        this.context = context;
        this.expression = expression;
    }

    public Object evaluate() {
        return evaluate(expression);
    }

    public Object evaluate(Expression expression) {
        Objects.requireNonNull(expression);
        if(expression instanceof UnaryExpression unaryExpression) {
            return evaluateUnary(unaryExpression);
        }
        if(expression instanceof BinaryExpression binaryExpression) {
            return evaluateBinary(binaryExpression);
        }
        if(expression instanceof ConstantExpression constantExpression) {
            return evaluateConst(constantExpression);
        }
        if(expression instanceof ArrayExpression listExpression) {
            return evaluateList(listExpression);
        }
        if(expression instanceof FunctionExpression functionExpression) {
            return evaluateFunction(functionExpression);
        }
        if(expression instanceof FieldExpression fieldExpression) {
            return evaluateField(fieldExpression);
        }
        if(context.isExpressionSupported(expression.getClass())) {
            return evaluateInContext(expression);
        }
        throw new RuntimeException("Unsupported expression: " + expression);
    }

    private Object evaluateField(FieldExpression fieldExpression) {
        Instance instance = (Instance) evaluate(fieldExpression.getInstance());
        return NncUtils.get(instance, inst -> inst.getResolved(fieldExpression.getFieldIds()));
    }

    private Object evaluateFunction(FunctionExpression functionExpression) {
        List<Object> argValues = NncUtils.map(functionExpression.getArguments(), this::evaluate);
        return functionExpression.getFunction().evaluate(argValues);
    }

    private Object evaluateList(ArrayExpression listExpression) {
        return NncUtils.map(
                listExpression.getExpressions(),
                this::evaluate
        );
    }

    private Object evaluateInContext(Expression expression) {
        return context.evaluate(expression, this);
    }

    private Object evaluateConst(ConstantExpression constantExpression) {
        return constantExpression.getValue();
    }

    private Object evaluateBinary(BinaryExpression binaryExpression) {
        Operator op = binaryExpression.getOperator();
        Object firstValue = evaluate(binaryExpression.getFirst()),
                secondValue = evaluate(binaryExpression.getSecond());
        if(op == Operator.EQ) {
            if(firstValue instanceof Instance instance && secondValue instanceof String title) {
                if(instance.getType().isEnum()) {
                    EnumConstant opt = NncUtils.find(
                            instance.getType().getEnumConstants(),
                            option -> Objects.equals(option.getId(), instance.getId())
                    );
                    String optionName = NncUtils.get(opt, EnumConstant::getName);
                    return Objects.equals(optionName, title);
                }
            }
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

//        TODO: 支持负数运算符
//        if(op == Operator.NEGATE) {
//            if(ValueUtil.isInteger(value)) {
//                return -castInteger(value);
//            }
//            return -castFloat(value);
//        }
        if(op == Operator.IS_NOT_NULL) {
            return value != null;
        }
        if(op == Operator.IS_NULL) {
            return value == null;
        }
        throw BusinessException.invalidExpression("不支持的运算符: " + expr.getOperator());
    }

}
