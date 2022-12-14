package tech.metavm.object.instance.query;

import tech.metavm.object.instance.ArrayInstance;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.StringInstance;
import tech.metavm.object.meta.EnumType;
import tech.metavm.util.BusinessException;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.List;
import java.util.Objects;

import static tech.metavm.object.instance.query.ExpressionUtil.*;
import static tech.metavm.util.ValueUtil.isCollection;

public class ExpressionEvaluator {

    public static Instance evaluate(Expression expression, ObjectTree objectTree) {
        return new ExpressionEvaluator(expression, new TreeEvaluationContext(objectTree)).evaluate();
    }

    public static Object evaluate(Expression expression, ClassInstance instance) {
        return new ExpressionEvaluator(expression, new InstanceEvaluationContext(instance)).evaluate();
    }

    public static Instance evaluate(Expression expression, EvaluationContext context) {
        return new ExpressionEvaluator(expression, context).evaluate();
    }

    private final EvaluationContext context;
    private final Expression expression;
    private final boolean containAsEqual;

    private ExpressionEvaluator(Expression expression, EvaluationContext context) {
        this(expression, context, false);
    }

    public ExpressionEvaluator(Expression expression, EvaluationContext context, boolean containAsEqual) {
        this.context = context;
        this.expression = expression;
        this.containAsEqual = containAsEqual;
    }

    public Instance evaluate() {
        return evaluate(expression);
    }

    public Instance evaluate(Expression expression) {
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

    private Instance evaluateField(FieldExpression fieldExpression) {
        ClassInstance instance = (ClassInstance) evaluate(fieldExpression.getInstance());
        return NncUtils.get(instance, inst -> inst.getResolved(fieldExpression.getFieldIds()));
    }

    private Instance evaluateFunction(FunctionExpression functionExpression) {
        List<Instance> argValues = NncUtils.map(functionExpression.getArguments(), this::evaluate);
        return functionExpression.getFunction().evaluate(argValues);
    }

    private ArrayInstance evaluateList(ArrayExpression listExpression) {
        return InstanceUtils.createArray(NncUtils.map(
                listExpression.getExpressions(),
                this::evaluate
        ));
    }

    private Instance evaluateInContext(Expression expression) {
        return context.evaluate(expression, this);
    }

    private Instance evaluateConst(ConstantExpression constantExpression) {
        return constantExpression.getValue();
    }

    private Instance evaluateBinary(BinaryExpression binaryExpression) {
        Operator op = binaryExpression.getOperator();
        Instance firstValue = evaluate(binaryExpression.getFirst()),
                secondValue = evaluate(binaryExpression.getSecond());
        if(op == Operator.EQ) {
            if(firstValue.getType() instanceof EnumType enumType) {
//                EnumConstantRT opt = NncUtils.find(
//                        enumType.getEnumConstants(),
//                        option -> Objects.equals(option.getId(), firstValue.getId())
//                );
                if(secondValue instanceof StringInstance title) {
                    ClassInstance classInstance = (ClassInstance) firstValue;
                    StringInstance optionName = classInstance.getString(classInstance.getType().getFieldByJavaField(
                            ReflectUtils.getField(Enum.class, "name")
                    ));
                    return InstanceUtils.equals(optionName, title);
                }
//                else if(secondValue instanceof Long id) {
//                    return Objects.equals(opt.getId(), id);
//                }
            }
            boolean equals = Objects.equals(firstValue, secondValue) ||
                    containAsEqual && isCollection(firstValue) && castCollection(firstValue).contains(secondValue);
            return InstanceUtils.createBoolean(equals);
        }
        if(op == Operator.NE) {
            return InstanceUtils.notEquals(firstValue, secondValue);
        }
        if(op == Operator.GE) {
            if(InstanceUtils.isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).isGreaterThanOrEqualTo(castInteger(secondValue));
            }
            return castFloat(firstValue).isGreaterThanOrEqualTo(castFloat(secondValue));
        }
        if(op == Operator.GT) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).isGreaterThan(castInteger(secondValue));
            }
            return castFloat(firstValue).isGreaterThan(castFloat(secondValue));
        }
        if(op == Operator.LE) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).isLessThanOrEqualTo(castInteger(secondValue));
            }
            return castFloat(firstValue).isLessThanOrEqualTo(castFloat(secondValue));
        }
        if(op == Operator.LT) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).isLessThan(castInteger(secondValue));
            }
            return castFloat(firstValue).isLessThan(castFloat(secondValue));
        }
        if(op == Operator.ADD) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).add(castInteger(secondValue));
            }
            return castFloat(firstValue).add(castFloat(secondValue));
        }
        if(op == Operator.SUBTRACT) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).subtract(castInteger(secondValue));
            }
            return castFloat(firstValue).subtract(castFloat(secondValue));
        }
        if(op == Operator.MULTIPLY) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).mul(castInteger(secondValue));
            }
            return castFloat(firstValue).mul(castFloat(secondValue));
        }
        if(op == Operator.DIVIDE) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).div(castInteger(secondValue));
            }
            return castFloat(firstValue).div(castFloat(secondValue));
        }
        if(op == Operator.MOD) {
            if(isAllInteger(firstValue, secondValue)) {
                return castInteger(firstValue).mod(castInteger(secondValue));
            }
            return castFloat(firstValue).mod(castFloat(secondValue));
        }
        if(op == Operator.STARTS_WITH) {
            return castString(firstValue).startsWith(castString(secondValue));
        }
        if(op == Operator.LIKE) {
            return castString(firstValue).contains(castString(secondValue));
        }
        if(op == Operator.IN) {
            return castCollection(secondValue).instanceContains(firstValue);
        }
        if(op == Operator.AND) {
            return castBoolean(firstValue).and(castBoolean(secondValue));
        }
        if(op == Operator.OR) {
            return castBoolean(firstValue).or(castBoolean(secondValue));
        }
        throw new RuntimeException("Unsupported operator " + op + " for binary expressions");
    }

    private Instance evaluateUnary(UnaryExpression expr) {
        Operator op = expr.getOperator();
        Instance value = evaluate(expr.getOperand());
        if(op == Operator.NOT) {
            return castBoolean(value).negate();
        }
//        TODO: 支持负数运算符
//        if(op == Operator.NEGATE) {
//            if(ValueUtil.isInteger(value)) {
//                return -castInteger(value);
//            }
//            return -castFloat(value);
//        }
        if(op == Operator.IS_NOT_NULL) {
            return InstanceUtils.createBoolean(!value.isNull());
        }
        if(op == Operator.IS_NULL) {
            return InstanceUtils.createBoolean(value == null);
        }
        throw BusinessException.invalidExpression("不支持的运算符: " + expr.getOperator());
    }

}
