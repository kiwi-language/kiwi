package tech.metavm.expression;

import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.ParentRef;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.query.ObjectNode;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.rest.dto.InstanceParentRef;
import tech.metavm.util.*;

import java.util.List;
import java.util.Objects;

import static tech.metavm.expression.ExpressionUtil.*;
import static tech.metavm.util.InstanceUtils.*;
import static tech.metavm.util.ValueUtil.isCollection;

public class ExpressionEvaluator {

    public static Instance evaluate(Expression expression, ObjectNode objectNode, Instance instance, IEntityContext entityContext) {
        return new ExpressionEvaluator(expression, new TreeEvaluationContext(objectNode, instance, entityContext)).evaluate();
    }

    public static Instance evaluate(Expression expression, ClassInstance instance, IEntityContext entityContext) {
        return new ExpressionEvaluator(expression, new InstanceEvaluationContext(instance, entityContext)).evaluate();
    }

    public static Instance evaluate(Expression expression, EvaluationContext context) {
        return new ExpressionEvaluator(expression, context).evaluate();
    }

    public static Instance evaluateChild(Expression expression, InstanceParentRef parentRef, EvaluationContext context) {
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
        if (context.isContextExpression(expression)) {
            return evaluateInContext(expression);
        }
        return switch (expression) {
            case UnaryExpression unaryExpression -> evaluateUnary(unaryExpression);
            case BinaryExpression binaryExpression -> evaluateBinary(binaryExpression);
            case ConstantExpression constantExpression -> evaluateConst(constantExpression);
            case ArrayExpression listExpression -> evaluateList(listExpression);
            case FunctionExpression functionExpression -> evaluateFunction(functionExpression);
            case PropertyExpression fieldExpression -> evaluateProperty(fieldExpression);
            case AllMatchExpression allMatchExpression -> evaluateAllMatch(allMatchExpression);
            case AsExpression asExpression -> evaluateAs(asExpression);
            case StaticFieldExpression staticFieldExpression -> evaluateStaticField(staticFieldExpression);
            case ArrayAccessExpression arrayAccExpression -> evaluateArrayAccess(arrayAccExpression);
            case InstanceOfExpression instanceOfExpression -> evaluateInstanceOf(instanceOfExpression);
            case FuncExpression funcExpression -> evaluateFunc(funcExpression);
            case null, default -> throw new RuntimeException("Unsupported expression: " + expression);
        };
    }

    private Instance evaluateFunc(FuncExpression funcExpression) {
        return ((ClassInstance) evaluate(funcExpression.getSelf())).getFunction(funcExpression.getFlow());
    }

    private Instance evaluateInstanceOf(InstanceOfExpression instanceOfExpression) {
        var instance = evaluate(instanceOfExpression.getOperand());
        return InstanceUtils.booleanInstance(instanceOfExpression.getTargetType().isInstance(instance));
    }

    private Instance evaluateArrayAccess(ArrayAccessExpression expression) {
        ArrayInstance array = (ArrayInstance) evaluate(expression.getArray());
        LongInstance index = (LongInstance) evaluate(expression.getIndex());
        return array.get(index.getValue().intValue());
    }

    private Instance evaluateAs(AsExpression expression) {
        return evaluate(expression.getExpression());
    }

    private Instance evaluateProperty(PropertyExpression expression) {
        ClassInstance instance = (ClassInstance) evaluate(expression.getInstance());
        return NncUtils.get(instance, inst -> inst.getProperty(expression.getProperty()));
    }

    private Instance evaluateFunction(FunctionExpression functionExpression) {
        List<Instance> args = NncUtils.map(functionExpression.getArguments(), this::evaluate);
        return functionExpression.getFunction().evaluate(args);
    }

    private Instance evaluateStaticField(StaticFieldExpression expression) {
        return expression.getField().getStaticValue();
    }

    private ArrayInstance evaluateList(ArrayExpression listExpression) {
        return InstanceUtils.createArray(NncUtils.map(
                listExpression.getExpressions(),
                this::evaluate
        ));
    }

    private BooleanInstance evaluateAllMatch(AllMatchExpression expression) {
        Instance instance = evaluate(expression.getArray());
        if (instance.isNull()) {
            return trueInstance();
        }
        if (!(instance instanceof ArrayInstance array)) {
            throw new InternalException("Expecting array instance for AllMatchExpression but got " + instance);
        }
        for (Instance element : array.getElements()) {
            if (element instanceof ClassInstance classInstance) {
                EvaluationContext subContext = new SubEvaluationContext(context, expression.getCursor(), classInstance);
                if (!InstanceUtils.isTrue(
                        evaluate(expression.getCondition(), subContext))
                ) {
                    return InstanceUtils.falseInstance();
                }
            } else {
                throw new InternalException("AllMatchExpression only supports reference array right now");
            }
        }
        return InstanceUtils.trueInstance();
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

        if (op == Operator.EQ) {
            if (firstValue.getType().getCategory() == TypeCategory.ENUM) {
//                EnumConstantRT opt = NncUtils.find(
//                        enumType.getEnumConstants(),
//                        option -> Objects.equals(option.getId(), firstValue.getId())
//                );
                if (secondValue instanceof StringInstance title) {
                    ClassInstance classInstance = (ClassInstance) firstValue;
                    StringInstance optionName = classInstance.getStringField(classInstance.getType().getFieldByJavaField(
                            ReflectUtils.getField(Enum.class, "name")
                    ));
                    return InstanceUtils.equals(optionName, title);
                }
//                else if(secondValue instanceof Long id) {
//                    return Objects.equals(opt.getId(), id);
//                }
            }
            boolean equals;
            if (isAllIntegers(firstValue, secondValue)) {
                equals = castInteger(firstValue).equals(castInteger(secondValue));
            } else if (isAllNumbers(firstValue, secondValue)) {
                equals = castFloat(firstValue).equals(castFloat(secondValue));
            } else {
                equals = Objects.equals(firstValue, secondValue) ||
                        containAsEqual && isCollection(firstValue) && castCollection(firstValue).contains(secondValue);
            }
            return InstanceUtils.createBoolean(equals);
        }
        if (op == Operator.NE) {
            boolean equals;
            if (isAllIntegers(firstValue, secondValue)) {
                equals = !castInteger(firstValue).equals(castInteger(secondValue));
            } else if (isAllNumbers(firstValue, secondValue)) {
                equals = !castFloat(firstValue).equals(castFloat(secondValue));
            } else {
                equals = !Objects.equals(firstValue, secondValue) &&
                        !(containAsEqual && isCollection(firstValue) && castCollection(firstValue).contains(secondValue));
            }
            return InstanceUtils.createBoolean(equals);
        }
        if (op == Operator.GE) {
            if (isAllTime(firstValue, secondValue)) {
                return ((TimeInstance) firstValue).isAfterOrAt((TimeInstance) secondValue);
            }
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).isGreaterThanOrEqualTo(castInteger(secondValue));
            }
            return castFloat(firstValue).isGreaterThanOrEqualTo(castFloat(secondValue));
        }
        if (op == Operator.GT) {
            if (isAllTime(firstValue, secondValue)) {
                return ((TimeInstance) firstValue).isAfter((TimeInstance) secondValue);
            }
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).isGreaterThan(castInteger(secondValue));
            }
            return castFloat(firstValue).isGreaterThan(castFloat(secondValue));
        }
        if (op == Operator.LE) {
            if (isAllTime(firstValue, secondValue)) {
                return ((TimeInstance) firstValue).isBeforeOrAt((TimeInstance) secondValue);
            }
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).isLessThanOrEqualTo(castInteger(secondValue));
            }
            return castFloat(firstValue).isLessThanOrEqualTo(castFloat(secondValue));
        }
        if (op == Operator.LT) {
            if (isAllTime(firstValue, secondValue)) {
                return ((TimeInstance) firstValue).isBefore((TimeInstance) secondValue);
            }
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).isLessThan(castInteger(secondValue));
            }
            return castFloat(firstValue).isLessThan(castFloat(secondValue));
        }
        if (op == Operator.ADD) {
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).add(castInteger(secondValue));
            }
            if (firstValue instanceof StringInstance || secondValue instanceof StringInstance) {
                return castString(firstValue).concat(castString(secondValue));
            }
            return castFloat(firstValue).add(castFloat(secondValue));
        }
        if (op == Operator.MINUS) {
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).minus(castInteger(secondValue));
            }
            return castFloat(firstValue).minus(castFloat(secondValue));
        }
        if (op == Operator.MULTIPLY) {
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).mul(castInteger(secondValue));
            }
            return castFloat(firstValue).mul(castFloat(secondValue));
        }
        if (op == Operator.DIVIDE) {
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).div(castInteger(secondValue));
            }
            return castFloat(firstValue).div(castFloat(secondValue));
        }
        if (op == Operator.MOD) {
            if (isAllIntegers(firstValue, secondValue)) {
                return castInteger(firstValue).mod(castInteger(secondValue));
            }
            return castFloat(firstValue).mod(castFloat(secondValue));
        }
        if (op == Operator.STARTS_WITH) {
            if (InstanceUtils.isAnyNull(firstValue, secondValue)) {
                return InstanceUtils.falseInstance();
            }
            return castString(firstValue).startsWith(castString(secondValue));
        }
        if (op == Operator.LIKE) {
            if (InstanceUtils.isAnyNull(firstValue, secondValue)) {
                return falseInstance();
            }
            return castString(firstValue).contains(castString(secondValue));
        }
        if (op == Operator.IN) {
            if (InstanceUtils.isAnyNull(firstValue, secondValue)) {
                return InstanceUtils.falseInstance();
            }
            return castCollection(secondValue).instanceContains(firstValue);
        }
        if (op == Operator.AND) {
            return castBoolean(firstValue).and(castBoolean(secondValue));
        }
        if (op == Operator.OR) {
            return castBoolean(firstValue).or(castBoolean(secondValue));
        }
        throw new RuntimeException("Unsupported operator " + op + " for binary expressions");
    }

    private Instance evaluateUnary(UnaryExpression expr) {
        Operator op = expr.getOperator();
        Instance value = evaluate(expr.getOperand());
        if (op == Operator.NOT) {
            return castBoolean(value).not();
        }
//        TODO: 支持负数运算符
//        if(op == Operator.NEGATE) {
//            if(ValueUtil.isInteger(value)) {
//                return -castInteger(value);
//            }
//            return -castFloat(value);
//        }
        if (op == Operator.IS_NOT_NULL) {
            return InstanceUtils.createBoolean(!value.isNull());
        }
        if (op == Operator.IS_NULL) {
            return InstanceUtils.createBoolean(value.isNull());
        }
        throw BusinessException.invalidExpression("不支持的运算符: " + expr.getOperator());
    }

}
