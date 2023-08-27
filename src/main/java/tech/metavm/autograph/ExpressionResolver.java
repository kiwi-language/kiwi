package tech.metavm.autograph;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import tech.metavm.expression.*;
import tech.metavm.flow.FlowRT;
import tech.metavm.flow.UpdateObjectNode;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.PrimitiveType;
import tech.metavm.object.meta.Type;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class ExpressionResolver {

    private static final Map<IElementType, Operator> OPERATOR_MAP = Map.of(
            JavaTokenType.PLUS, Operator.ADD,
            JavaTokenType.MINUS, Operator.SUBTRACT,
            JavaTokenType.PLUSPLUS, Operator.PLUS_PLUS,
            JavaTokenType.MINUSMINUS, Operator.MINUS_MINUS,
            JavaTokenType.EQEQ, Operator.EQ,
            JavaTokenType.GT, Operator.GT,
            JavaTokenType.GE, Operator.GE,
            JavaTokenType.LT, Operator.LT,
            JavaTokenType.LE, Operator.LE,
            JavaTokenType.ANDAND, Operator.AND
    );

    private final FlowBuilder flowBuilder;
    private final TypeResolver typeResolver;
    private final VariableTable variableTable;

    public ExpressionResolver(FlowBuilder flowBuilder, VariableTable variableTable, TypeResolver typeResolver) {
        this.flowBuilder = flowBuilder;
        this.typeResolver = typeResolver;
        this.variableTable = variableTable;
    }

    public Expression resolve(PsiExpression psiExpression) {
        return switch (psiExpression) {
            case PsiBinaryExpression binaryExpression -> resolveBinary(binaryExpression);
            case PsiUnaryExpression unaryExpression -> resolveUnary(unaryExpression);
            case PsiMethodCallExpression methodCallExpression -> resolveMethodCall(methodCallExpression);
            case PsiReferenceExpression referenceExpression -> resolveReference(referenceExpression);
            case PsiAssignmentExpression assignmentExpression -> resolveAssignment(assignmentExpression);
            case PsiLiteralExpression literalExpression -> resolveLiteral(literalExpression);
            default -> throw new IllegalStateException("Unexpected value: " + psiExpression);
        };
    }

    private Expression resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        Instance instance;
        PrimitiveType valueType = (PrimitiveType) typeResolver.resolve(literalExpression.getType());
        if(value == null) {
            instance = new NullInstance(valueType);
        }
        else if(value instanceof Boolean boolValue) {
            instance = new BooleanInstance(boolValue, valueType);
        }
        else if(value instanceof Integer integer) {
            instance = new IntInstance(integer, valueType);
        }
        else if(value instanceof Float floatValue) {
            instance = new DoubleInstance(floatValue.doubleValue(), valueType);
        }
        else if(value instanceof Double doubleValue) {
            instance = new DoubleInstance(doubleValue, valueType);
        }
        else if(value instanceof Long longValue) {
            instance = new LongInstance(longValue, valueType);
        }
        else throw new InternalException("Unrecognized literal value: " + value);

        return new ConstantExpression(instance);
    }

    private Expression resolveReference(PsiReferenceExpression psiReferenceExpression) {
        var target = psiReferenceExpression.resolve();
        if (target instanceof PsiField psiField) {
            PsiClass psiClass = requireNonNull(psiField.getContainingClass());
            ClassType klass = (ClassType) typeResolver.resolve(TranspileUtil.getRawType(psiClass));
            Field field = klass.getFieldByCode(psiField.getName());
            var qualifier = psiReferenceExpression.getQualifierExpression();
            Expression qualifierExpr;
            if (qualifier == null) {
                qualifierExpr = variableTable.get("this");
            } else {
                qualifierExpr = resolve(qualifier);
            }
            return new FieldExpression(qualifierExpr, field);
        } else if (target instanceof PsiVariable variable) {
            return variableTable.get(variable.getName());
        }
        else throw new InternalException("Can not resolve reference expression with target: " + target);
    }

    private UnaryExpression resolveUnary(PsiUnaryExpression psiUnaryExpression) {
        return new UnaryExpression(
                resolveOperator(psiUnaryExpression.getOperationSign()),
                resolve(Objects.requireNonNull(psiUnaryExpression.getOperand()))
        );
    }

    private BinaryExpression resolveBinary(PsiBinaryExpression psiExpression) {
        return new BinaryExpression(
                resolveOperator(psiExpression.getOperationSign()),
                resolve(psiExpression.getLOperand()),
                resolve(Objects.requireNonNull(psiExpression.getROperand()))
        );
    }

    private NodeExpression resolveMethodCall(PsiMethodCallExpression expression) {
        PsiMethodReferenceExpression ref = (PsiMethodReferenceExpression) expression.getMethodExpression();
        PsiExpression psiSelf = (PsiExpression) ref.getQualifier();
        Expression self = resolve(requireNonNull(psiSelf));
        PsiMethod method = (PsiMethod) ref.resolve();
        List<Type> paramTypes = NncUtils.map(
                NncUtils.map(requireNonNull(method).getParameterList().getParameters(), PsiParameter::getType),
                typeResolver::resolve
        );
        ClassType selfType = (ClassType) self.getType();
        FlowRT flow = selfType.getFlow(method.getName(), paramTypes);
        List<Expression> args = NncUtils.map(
                expression.getArgumentList().getExpressions(),
                this::resolve
        );
        var subFlowNode = flowBuilder.createSubFlow(self, flow, args);
        return new NodeExpression(subFlowNode);
    }

    private Expression resolveAssignment(PsiAssignmentExpression expression) {
        var op = expression.getOperationSign().getTokenType();
        var assigned = expression.getLExpression();
        var assignment = resolve(requireNonNull(expression.getRExpression()));
        if (assigned instanceof PsiReferenceExpression referenceExpression) {
            var target = referenceExpression.resolve();
            if (target instanceof PsiVariable variable) {
                if (variable instanceof PsiField field) {
                    Expression self;
                    if(referenceExpression.getQualifierExpression() != null) {
                        self = resolve(referenceExpression.getQualifierExpression());
                    }
                    else {
                        self = variableTable.get("this");
                    }
                    processFieldAssignment(self, field, assignment);
                } else {
                    if(op == JavaTokenType.EQ) {
                        variableTable.set(variable.getName(), assignment);
                    }
                    else if(op == JavaTokenType.PLUSEQ) {
                        variableTable.set(
                                variable.getName(),
                                new BinaryExpression(
                                        Operator.ADD,
                                        variableTable.get(variable.getName()),
                                        assignment
                                )
                        );
                    }
                }
            }
        }
        return assignment;
    }

    private ThisExpression createThisExpression(PsiClass psiClass) {
        return new ThisExpression((ClassType) typeResolver.resolve(TranspileUtil.getTemplateType(psiClass)));
    }

    private void processFieldAssignment(Expression instanceExpr, PsiField psiField, Expression value) {
        ClassType instanceType = (ClassType) instanceExpr.getType();
        Field field = instanceType.getFieldByCode(psiField.getName());
        UpdateObjectNode node = flowBuilder.createUpdateObject();
        node.setObjectId(instanceExpr);
        node.setUpdateField(field, value);
    }

    private Operator resolveOperator(PsiJavaToken psiOp) {
        return OPERATOR_MAP.get(psiOp.getTokenType());
    }

}
