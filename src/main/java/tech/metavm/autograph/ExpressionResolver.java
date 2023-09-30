package tech.metavm.autograph;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.*;

import java.util.*;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.matchClass;
import static tech.metavm.autograph.TranspileUtil.matchMethod;
import static tech.metavm.util.ReflectUtils.getMethod;

public class ExpressionResolver {

    private static final Map<IElementType, Operator> OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.PLUS, Operator.ADD),
            Map.entry(JavaTokenType.MINUS, Operator.MINUS),
            Map.entry(JavaTokenType.PLUSPLUS, Operator.PLUS_PLUS),
            Map.entry(JavaTokenType.MINUSMINUS, Operator.MINUS_MINUS),
            Map.entry(JavaTokenType.EQEQ, Operator.EQ),
            Map.entry(JavaTokenType.NE, Operator.NE),
            Map.entry(JavaTokenType.GT, Operator.GT),
            Map.entry(JavaTokenType.GE, Operator.GE),
            Map.entry(JavaTokenType.LT, Operator.LT),
            Map.entry(JavaTokenType.LE, Operator.LE),
            Map.entry(JavaTokenType.ANDAND, Operator.AND),
            Map.entry(JavaTokenType.OROR, Operator.OR),
            Map.entry(JavaTokenType.ASTERISK, Operator.MULTIPLY),
            Map.entry(JavaTokenType.DIV, Operator.DIVIDE)
    );

    public static final Set<IElementType> BOOL_OPS = Set.of(
            JavaTokenType.ANDAND, JavaTokenType.OROR
    );

    private final Generator visitor;
    private final FlowGenerator flowBuilder;
    private final TypeResolver typeResolver;
    private final VariableTable variableTable;
    private final IEntityContext entityContext;

    public ExpressionResolver(Generator visitor, FlowGenerator flowBuilder, VariableTable variableTable, TypeResolver typeResolver, IEntityContext entityContext) {
        this.visitor = visitor;
        this.flowBuilder = flowBuilder;
        this.typeResolver = typeResolver;
        this.variableTable = variableTable;
        this.entityContext = entityContext;
    }

    public Expression resolve(PsiExpression psiExpression) {
        ResolutionContext context = new ResolutionContext();
        return resolve(psiExpression, context);
    }

    private Expression resolve(PsiExpression psiExpression, ResolutionContext context) {
        if (isBoolExpression(psiExpression)) {
            return resolveBool(psiExpression, context);
        } else {
            return resolveNormal(psiExpression, context);
        }
    }

    private Expression resolveNormal(PsiExpression psiExpression, ResolutionContext context) {
        return switch (psiExpression) {
            case PsiBinaryExpression binaryExpression -> resolveBinary(binaryExpression, context);
            case PsiUnaryExpression unaryExpression -> resolveUnary(unaryExpression, context);
            case PsiMethodCallExpression methodCallExpression -> resolveMethodCall(methodCallExpression, context);
            case PsiNewExpression newExpression -> resolveNew(newExpression, context);
            case PsiReferenceExpression referenceExpression -> resolveReference(referenceExpression, context);
            case PsiAssignmentExpression assignmentExpression -> resolveAssignment(assignmentExpression, context);
            case PsiLiteralExpression literalExpression -> resolveLiteral(literalExpression);
            case PsiThisExpression thisExpression -> resolveThis(thisExpression);
            case PsiParenthesizedExpression parExpression -> resolveParenthesized(parExpression, context);
            case PsiConditionalExpression conditionalExpression -> resolveConditional(conditionalExpression, context);
            case PsiArrayAccessExpression arrayAccessExpression -> resolveArrayAccess(arrayAccessExpression, context);
            default -> throw new IllegalStateException("Unexpected value: " + psiExpression);
        };
    }

    private Expression resolveArrayAccess(PsiArrayAccessExpression arrayAccessExpression, ResolutionContext context) {
        return new ArrayAccessExpression(
                resolve(arrayAccessExpression.getArrayExpression(), context),
                resolve(arrayAccessExpression.getIndexExpression(), context)
        );
    }

    private boolean isBoolExpression(PsiExpression psiExpression) {
        return psiExpression.getType() == PsiType.BOOLEAN;
    }

    private Expression resolveThis(PsiThisExpression ignored) {
        return variableTable.get("this");
    }

    private Expression resolveConditional(PsiConditionalExpression psiExpression, ResolutionContext context) {
        return new FunctionExpression(
                Function.IF,
                new ArrayExpression(
                        List.of(
                                resolveBool(psiExpression.getCondition(), context),
                                resolve(requireNonNull(psiExpression.getThenExpression()), context),
                                resolve(requireNonNull(psiExpression.getElseExpression()), context)
                        )
                )
        );
    }

    private Expression resolveParenthesized(PsiParenthesizedExpression psiExpression, ResolutionContext context) {
        return resolve(requireNonNull(psiExpression.getExpression()), context);
    }

    private Expression resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        if (value == null) {
            return new ConstantExpression(InstanceUtils.nullInstance());
        }
        Instance instance;
        PrimitiveType valueType = (PrimitiveType) typeResolver.resolve(literalExpression.getType());
        instance = switch (value) {
            case Boolean boolValue -> new BooleanInstance(boolValue, valueType);
            case Integer integer -> new LongInstance(integer, valueType);
            case Float floatValue -> new DoubleInstance(floatValue.doubleValue(), valueType);
            case Double doubleValue -> new DoubleInstance(doubleValue, valueType);
            case Long longValue -> new LongInstance(longValue, valueType);
            case String string -> InstanceUtils.stringInstance(string);
            case null, default -> throw new InternalException("Unrecognized literal value: " + value);
        };
        return new ConstantExpression(instance);
    }

    private Expression resolveReference(PsiReferenceExpression psiReferenceExpression, ResolutionContext context) {
        var qnAndMode = psiReferenceExpression.getUserData(Keys.QN_AND_MODE);
        if (qnAndMode != null) {
            var qn = qnAndMode.qualifiedName();
            var tmpValue = context.get(qn);
            if (tmpValue != null) {
                return tmpValue;
            }
        }
        var target = psiReferenceExpression.resolve();
        if (target instanceof PsiField psiField) {
            if (isArrayLength(psiField)) {
                var arrayExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                return new FunctionExpression(Function.LEN, arrayExpr);
            } else {
                PsiClass psiClass = requireNonNull(psiField.getContainingClass());
                Field field;
                if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                    ClassType klass = (ClassType) typeResolver.resolveDeclaration(TranspileUtil.getRawType(psiClass));
                    field = klass.getStaticFieldByCode(psiField.getName());
                    return new StaticFieldExpression(field);
                } else {
                    var qualifierExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    ClassType klass = (ClassType) qualifierExpr.getType();
                    typeResolver.ensureDeclared(klass);
                    field = klass.getFieldByCode(psiField.getName());
                    return new FieldExpression(qualifierExpr, field);
                }
            }
        } else if (target instanceof PsiVariable variable) {
            return variableTable.get(variable.getName());
        } else {
            throw new InternalException("Can not resolve reference expression with target: " + target);
        }
    }

    private Expression resolveQualifier(PsiExpression qualifier, ResolutionContext context) {
        if (qualifier == null) {
            return variableTable.get("this");
        } else {
            return resolve(qualifier, context);
        }
    }

    private boolean isArrayLength(PsiField field) {
        return field.getName().equals("length") &&
                Objects.equals(requireNonNull(field.getContainingClass()).getName(), "__Array__");
    }

    private Expression resolvePrefix(PsiPrefixExpression psiPrefixExpression, ResolutionContext context) {
        var operand = (PsiReferenceExpression) NncUtils.requireNonNull(psiPrefixExpression.getOperand());
        var op = psiPrefixExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EXCL) {
            return new BinaryExpression(
                    Operator.EQ,
                    resolveBool(Objects.requireNonNull(psiPrefixExpression.getOperand()), context),
                    new ConstantExpression(InstanceUtils.booleanInstance(false))
            );
        }
        var resolvedOperand = resolve(requireNonNull(psiPrefixExpression.getOperand()), context);
        if (op == JavaTokenType.PLUSPLUS) {
            return processAssignment(
                    operand,
                    new BinaryExpression(
                            Operator.ADD,
                            resolvedOperand,
                            new ConstantExpression(InstanceUtils.longInstance(1))
                    ),
                    context
            );
        } else if (op == JavaTokenType.MINUSMINUS) {
            return processAssignment(
                    operand,
                    new BinaryExpression(
                            Operator.MINUS,
                            resolvedOperand,
                            new ConstantExpression(InstanceUtils.longInstance(1))
                    ),
                    context
            );
        } else {
            throw new InternalException("Unsupported prefix operator " + op);
        }
    }

    private Expression resolveUnary(PsiUnaryExpression psiUnaryExpression, ResolutionContext context) {
        if (psiUnaryExpression instanceof PsiPrefixExpression prefixExpression) {
            return resolvePrefix(prefixExpression, context);
        }
        var op = psiUnaryExpression.getOperationSign().getTokenType();
        var resolvedOperand = resolve(requireNonNull(psiUnaryExpression.getOperand()), context);
        if (op == JavaTokenType.PLUSPLUS) {
            processAssignment(
                    (PsiReferenceExpression) psiUnaryExpression.getOperand(),
                    new BinaryExpression(
                            Operator.ADD,
                            resolvedOperand,
                            new ConstantExpression(InstanceUtils.longInstance(1))
                    ),
                    context
            );
            return resolvedOperand;
        } else if (op == JavaTokenType.MINUSMINUS) {
            processAssignment(
                    (PsiReferenceExpression) psiUnaryExpression.getOperand(),
                    new BinaryExpression(
                            Operator.MINUS,
                            resolvedOperand,
                            new ConstantExpression(InstanceUtils.longInstance(1))
                    ),
                    context
            );
            return resolvedOperand;
        } else {
            return new UnaryExpression(
                    resolveOperator(psiUnaryExpression.getOperationSign()),
                    resolve(Objects.requireNonNull(psiUnaryExpression.getOperand()), context)
            );
        }
    }

    private Expression resolveBinary(PsiBinaryExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationSign().getTokenType();
        if (BOOL_OPS.contains(op)) {
            return new BinaryExpression(
                    resolveOperator(psiExpression.getOperationSign()),
                    resolveBool(psiExpression.getLOperand(), context),
                    resolveBool(Objects.requireNonNull(psiExpression.getROperand()), context)
            );
        } else {
            return new BinaryExpression(
                    resolveOperator(psiExpression.getOperationSign()),
                    resolve(psiExpression.getLOperand(), context),
                    resolve(Objects.requireNonNull(psiExpression.getROperand()), context)
            );
        }
    }

    private Expression resolveMethodCall(PsiMethodCallExpression expression, ResolutionContext context) {
        var ref = expression.getMethodExpression();
        var method = requireNonNull(expression.resolveMethod());
        var klass = requireNonNull(method.getContainingClass());
        var argumentList = expression.getArgumentList();
        if (TranspileUtil.matchClass(klass, List.class)) {
            var array = resolveQualifier(ref.getQualifierExpression(), context);
            if (matchMethod(method, getMethod(List.class, "size"))) {
                return invokeFlow(array, "size", argumentList, context);
            } else if (matchMethod(method, getMethod(List.class, "add", Object.class))) {
                createInvokeFlowNode(array, "add", argumentList, context);
                return ExpressionUtil.trueExpression();
            } else if (matchMethod(method, getMethod(List.class, "remove", Object.class))) {
                return invokeFlow(array, "remove", argumentList, context);
            } else if (matchMethod(method, getMethod(List.class, "get", int.class))) {
                return invokeFlow(array, "get", argumentList, context);
            } else if (matchMethod(method, getMethod(List.class, "set", int.class, Object.class))) {
                return invokeFlow(array, "set", argumentList, context);
            } else if (matchMethod(method, getMethod(List.class, "remove", int.class))) {
                return invokeFlow(array, "removeAt", argumentList, context);
            } else if (matchMethod(method, getMethod(List.class, "clear"))) {
                invokeFlow(array, "clear", expression.getArgumentList(), context);
                return null;
            } else {
                throw new InternalException("Unsupported List method: " + method);
            }
        }
        if (matchClass(klass, Map.class)) {
            var map = resolveQualifier(ref.getQualifierExpression(), context);
            if (matchMethod(method, getMethod(Map.class, "get", Object.class))) {
                return invokeFlow(map, "get", argumentList, context);
            } else if (matchMethod(method, getMethod(Map.class, "put", Object.class, Object.class))) {
                return invokeFlow(map, "put", argumentList, context);
            } else if (matchMethod(method, getMethod(Map.class, "remove", Object.class))) {
                return invokeFlow(map, "remove", argumentList, context);
            } else if (matchMethod(method, getMethod(Map.class, "size"))) {
                return invokeFlow(map, "size", argumentList, context);
            } else if (matchMethod(method, getMethod(Map.class, "clear"))) {
                return invokeFlow(map, "clear", argumentList, context);
            } else {
                throw new InternalException("Unsupported Map method: " + method);
            }
        } else {
            return resolveFlowCall(expression, context);
        }
    }

    private SubFlowNode createInvokeFlowNode(Expression self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
        List<Expression> arguments = NncUtils.map(argumentList.getExpressions(), arg -> resolve(arg, context));
        var exprType = (ClassType) flowBuilder.getExpressionType(self);
        return flowBuilder.createSubFlow(self, exprType.getFlowByCode(flowCode), arguments);
    }

    private Expression invokeFlow(Expression self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
        return getSingleValue(createInvokeFlowNode(self, flowCode, argumentList, context));
    }

    private void ensureTypeDeclared(PsiType psiType) {
        switch (psiType) {
            case PsiClassType classType -> {
                var psiClass = classType.resolve();
                if (psiClass instanceof PsiTypeParameter typeParameter) {
                    for (PsiClassType bound : typeParameter.getExtendsListTypes()) {
                        ensureTypeDeclared(bound);
                    }
                } else {
                    typeResolver.resolveDeclaration(classType);
                }
            }
            case PsiArrayType arrayType -> ensureTypeDeclared(arrayType.getComponentType());
            default -> throw new IllegalStateException("Unexpected value: " + psiType);
        }
    }

    private Expression resolveFlowCall(PsiMethodCallExpression expression, ResolutionContext context) {
        var ref = expression.getMethodExpression();
        PsiExpression psiSelf = (PsiExpression) ref.getQualifier();
        if (psiSelf != null) {
            ensureTypeDeclared(NncUtils.requireNonNull(psiSelf.getType()));
        }
        Expression self;
        if (psiSelf == null) self = variableTable.get("this");
        else self = resolve(requireNonNull(psiSelf), context);
        var exprType = flowBuilder.getExpressionType(self);
        ClassType selfType;
        if (exprType instanceof ClassType classType) {
            selfType = classType;
        } else if (exprType instanceof TypeVariable typeVariable) {
            selfType = (ClassType) typeVariable.getBounds().get(0);
        } else {
            throw new InternalException("Unexpected self expression type: " + exprType);
        }
        Flow flow = resolveFlow(selfType, expression);
        List<Expression> args = NncUtils.map(
                expression.getArgumentList().getExpressions(),
                expr -> resolve(expr, context)
        );
        var node = flowBuilder.createSubFlow(self, flow, args);
        if (flow.getOutputType().isVoid()) {
            return null;
        } else {
            return getSingleValue(node);
        }
    }

    private Flow resolveFlow(ClassType declaringType, PsiCallExpression expression) {
        var methodGenerics = expression.resolveMethodGenerics();
        var method = (PsiMethod) methodGenerics.getElement();
        List<PsiType> paramPsiTypes = NncUtils.map(
                requireNonNull(method).getParameterList().getParameters(),
                PsiParameter::getType
        );
        if (expression instanceof PsiMethodCallExpression methodCallExpression) {
            var methodExpr = methodCallExpression.getMethodExpression();
            var qualifierExpr = methodExpr.getQualifierExpression();
            if (qualifierExpr != null) {
                var selfType = NncUtils.requireNonNull(qualifierExpr.getType());
                if (selfType instanceof PsiClassType classType) {
                    var selfSubstitutor = classType.resolveGenerics().getSubstitutor();
                    paramPsiTypes = NncUtils.map(
                            paramPsiTypes,
                            selfSubstitutor::substitute
                    );
                }
            }
        }
        List<Type> paramTypes = NncUtils.map(paramPsiTypes, typeResolver::resolveDeclaration);
        var substitutor = methodGenerics.getSubstitutor();
        var flow = declaringType.getFlow(method.getName(), paramTypes);
        if (!flow.getTypeParameters().isEmpty()) {
            List<Type> typeArguments = NncUtils.map(
                    method.getTypeParameters(),
                    typeParam -> typeResolver.resolveDeclaration(substitutor.substitute(typeParam))
            );
            flow = entityContext.getGenericContext().getParameterizedFlow(flow, typeArguments);
        }
        return flow;
    }

    private Expression resolveNew(PsiNewExpression expression, ResolutionContext context) {
        if (expression.isArrayCreation()) {
            return resolveNewArray(expression, context);
        } else {
            return resolveNewPojo(expression, context);
        }
    }

    private Expression resolveNewArray(PsiNewExpression expression, ResolutionContext context) {
        if (expression.getArrayInitializer() == null) {
            throw new InternalException("Dimension new array expression not supported yet");
        }
        var node = flowBuilder.createNewArray(
                (ArrayType) typeResolver.resolveDeclaration(expression.getType()),
                NncUtils.map(
                        expression.getArrayInitializer().getInitializers(),
                        this::resolve
                )
        );
        return new NodeExpression(node);
    }

    private Expression resolveNewPojo(PsiNewExpression expression, ResolutionContext context) {
        var type = NncUtils.requireNonNull(expression.getType());
        var psiClass = requireNonNull(((PsiClassType) type).resolve());
        var psiListType = TranspileUtil.createType(List.class);
        if (psiListType.isAssignableFrom(type)) {
            var listType = (ClassType) typeResolver.resolve(type);
            var subFlow = listType.getFlowByCode("List");
            Expression elementAsChild = ExpressionUtil.falseExpression();
            if (Objects.equals(psiClass.getQualifiedName(), Table.class.getName())) {
                var argList = requireNonNull(expression.getArgumentList());
                if (argList.getExpressionCount() > 1) {
                    elementAsChild = resolve(argList.getExpressions()[1], context);
                }
            }
            return getSingleValue(flowBuilder.createNew(subFlow, List.of(elementAsChild)));
        }
        var psiMapType = TranspileUtil.createType(Map.class);
        if (psiMapType.isAssignableFrom(type)) {
            var mapType = (ClassType) typeResolver.resolve(type);
            var subFlow = mapType.getFlowByCode("Map");
            Expression valueAsChild = ExpressionUtil.falseExpression();
            var childHashMapType = TranspileUtil.createType(ChildHashMap.class);
            if (childHashMapType.isAssignableFrom(type)) {
                valueAsChild = ExpressionUtil.trueExpression();
            }
            return getSingleValue(flowBuilder.createNew(subFlow,
                    List.of(ExpressionUtil.falseExpression(), valueAsChild)
            ));
        }
        if (expression.getType() instanceof PsiClassType psiClassType) {
            var klass = (ClassType) typeResolver.resolve(psiClassType);
            List<Expression> args = NncUtils.map(
                    requireNonNull(expression.getArgumentList()).getExpressions(),
                    expr -> resolve(expr, context)
            );
            return newInstance(klass, args, List.of(), expression);
        } else {
            // TODO support new array instance
            throw new InternalException("Unsupported NewExpression: " + expression);
        }
    }

    public Expression newInstance(ClassType declaringType, List<Expression> arguments,
                                  List<PsiType> prefixTypes, PsiConstructorCall constructorCall) {
        var methodGenerics = constructorCall.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var substitutor = methodGenerics.getSubstitutor();
        var prefixParamTypes = NncUtils.map(
                prefixTypes,
                type -> typeResolver.resolveTypeOnly(substitutor.substitute(type))
        );
        var paramTypes = NncUtils.map(
                requireNonNull(method.getParameterList()).getParameters(),
                param -> typeResolver.resolveTypeOnly(substitutor.substitute(param.getType()))
        );
        paramTypes = NncUtils.merge(prefixParamTypes, paramTypes);
        var flow = declaringType.getFlow(declaringType.getCode(), paramTypes);
        var newNode = flowBuilder.createNew(flow, arguments);
        return new NodeExpression(newNode);
    }

    private Expression getSingleValue(NodeRT<?> node) {
        return new NodeExpression(node);
    }

    private Expression resolveAssignment(PsiAssignmentExpression expression, ResolutionContext context) {
        var op = expression.getOperationSign().getTokenType();
        var resolvedRight = resolve(requireNonNull(expression.getRExpression()), context);
        Expression assignment;
        if (op == JavaTokenType.EQ) {
            assignment = resolvedRight;
        } else {
            var resolvedLeft = resolve(expression.getLExpression(), context);
            if (op == JavaTokenType.PLUSEQ) {
                assignment = new BinaryExpression(Operator.ADD, resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.MINUSEQ) {
                assignment = new BinaryExpression(Operator.MINUS, resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.ASTERISKEQ) {
                assignment = new BinaryExpression(Operator.MULTIPLY, resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.DIVEQ) {
                assignment = new BinaryExpression(Operator.DIVIDE, resolvedLeft, resolvedRight);
            } else {
                throw new InternalException("Unsupported assignment operator " + op);
            }
        }
        return processAssignment(
                (PsiReferenceExpression) expression.getLExpression(), assignment, context
        );
    }

    private Expression processAssignment(PsiReferenceExpression assigned, Expression assignment, ResolutionContext context) {
        var target = assigned.resolve();
        if (target instanceof PsiVariable variable) {
            if (variable instanceof PsiField psiField) {
                Expression self;
                if (assigned.getQualifierExpression() != null) {
                    self = resolve(assigned.getQualifierExpression(), context);
                } else {
                    self = variableTable.get("this");
                }
                ClassType instanceType = (ClassType) flowBuilder.getExpressionType(self);
                Field field = instanceType.getFieldByCode(psiField.getName());
                UpdateObjectNode node = flowBuilder.createUpdateObject();
                node.setObjectId(new ExpressionValue(self));
                node.setUpdateField(field, UpdateOp.SET, new ExpressionValue(assignment));
            } else {
                variableTable.set(
                        variable.getName(),
                        new NodeExpression(flowBuilder.createValue(variable.getName(), assignment))
                );
            }
        } else {
            throw new InternalException("Invalid assignment target " + target);
        }
        return assignment;
    }

    private Operator resolveOperator(PsiJavaToken psiOp) {
        return OPERATOR_MAP.get(psiOp.getTokenType());
    }

    public Expression resolveBool(PsiExpression expression, ResolutionContext context) {
        return switch (expression) {
            case PsiBinaryExpression binaryExpression -> buildBinaryBool(binaryExpression, context);
            case PsiUnaryExpression unaryExpression -> buildUnaryBool(unaryExpression, context);
            default -> resolveNormal(expression, context);
        };
    }

    private Expression buildUnaryBool(PsiUnaryExpression unaryExpression, ResolutionContext context) {
        var operand = NncUtils.requireNonNull(unaryExpression.getOperand());
        var operandExpr = resolveBool(operand, context);
        var op = unaryExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EXCL) {
            return new UnaryExpression(Operator.NOT, operandExpr);
        } else {
            return resolveUnary(unaryExpression, context);
        }
    }

    public Expression buildBinaryBool(PsiBinaryExpression psiBinaryExpression, ResolutionContext context) {
        var op = psiBinaryExpression.getOperationSign().getTokenType();
        var first = NncUtils.requireNonNull(psiBinaryExpression.getLOperand());
        var second = NncUtils.requireNonNull(psiBinaryExpression.getROperand());
        if (op == JavaTokenType.ANDAND) {
            var firstExpr = resolveBool(first, context);
            var branchNode = flowBuilder.createBranch(false);
            var thenBranch = branchNode.addBranch(new ExpressionValue(firstExpr));
            var elseBranch = branchNode.addDefaultBranch();
            var trueScope = thenBranch.getScope();
            flowBuilder.enterScope(trueScope, firstExpr);
            var secondExpr = resolveBool(second, context);
            flowBuilder.exitScope();
            return merge(thenBranch, secondExpr, elseBranch, ExpressionUtil.falseExpression());
        } else if (op == JavaTokenType.OROR) {
            var firstExpr = resolveBool(first, context);
            var branchNode = flowBuilder.createBranch(false);
            var thenBranch = branchNode.addBranch(new ExpressionValue(firstExpr));
            var elseBranch = branchNode.addDefaultBranch();
            flowBuilder.enterScope(elseBranch.getScope(), ExpressionUtil.not(firstExpr));
            var secondExpr = resolveBool(second, context);
            flowBuilder.exitScope();
            return merge(thenBranch, ExpressionUtil.trueExpression(), elseBranch, secondExpr);
        } else {
            return resolveBinary(psiBinaryExpression, context);
        }
    }

    private Expression merge(Branch trueBranch, Expression trueBranchValue,
                             Branch falseBranch, Expression falseBranchValue) {
        var mergeNode = flowBuilder.createMerge();
        var valueField = FieldBuilder.newBuilder(
                "值",
                "value",
                mergeNode.getType(),
                ModelDefRegistry.getType(boolean.class)
        ).build();
        var valueMergeField = new MergeNodeField(valueField, mergeNode);
        valueMergeField.setValues(Map.of(
                trueBranch, new ExpressionValue(trueBranchValue),
                falseBranch, new ExpressionValue(falseBranchValue)
        ));
        return new FieldExpression(new NodeExpression(mergeNode), valueField);
    }


    public static class ResolutionContext {
        private final Map<QualifiedName, Expression> tempVariableMap = new HashMap<>();
        private final List<VariableOperation> variableOperations = new ArrayList<>();
        private final List<FieldOperation> fieldOperations = new ArrayList<>();
        private Expression result;

        public Expression get(QualifiedName qn) {
            return tempVariableMap.get(qn);
        }

        public void set(QualifiedName qn, Expression expression) {
            tempVariableMap.put(qn, expression);
        }

        public void finish(VariableTable variableTable, FlowGenerator flowBuilder) {
            for (VariableOperation(String variableName, Expression value) : variableOperations) {
                var node = flowBuilder.createValue(variableName, value);
                variableTable.set(variableName, new NodeExpression(node));
            }
            for (FieldOperation(Expression instance, Field field, Expression value) : fieldOperations) {
                var node = flowBuilder.createUpdateObject();
                node.setObjectId(new ExpressionValue(instance));
                node.setUpdateField(field, UpdateOp.SET, new ExpressionValue(value));
            }
        }

        public Expression getResult() {
            return result;
        }

        public void setResult(Expression result) {
            this.result = result;
        }

        @SuppressWarnings("unused")
        void addFieldOperation(FieldOperation fieldOperation) {
            fieldOperations.add(fieldOperation);
        }

        @SuppressWarnings("unused")
        void addVariableOperation(VariableOperation variableOperation) {
            variableOperations.add(variableOperation);
        }

    }

    private record VariableOperation(
            String variableName,
            Expression value
    ) {

    }

    private record FieldOperation(
            Expression instance,
            Field field,
            Expression value
    ) {

    }

}
