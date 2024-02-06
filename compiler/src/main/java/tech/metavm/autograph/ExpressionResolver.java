package tech.metavm.autograph;

import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.*;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.DoubleInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.LongInstance;
import tech.metavm.object.type.*;
import tech.metavm.util.Instances;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static tech.metavm.autograph.TranspileUtil.matchClass;
import static tech.metavm.autograph.TranspileUtil.matchMethod;
import static tech.metavm.util.ReflectionUtils.getMethod;

public class ExpressionResolver {

    public static final Map<IElementType, UnaryOperator> UNARY_OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.PLUS, UnaryOperator.POS),
            Map.entry(JavaTokenType.MINUS, UnaryOperator.NEG),
            Map.entry(JavaTokenType.EXCL, UnaryOperator.NOT)
    );

    private static final Map<IElementType, BinaryOperator> OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.PLUS, BinaryOperator.ADD),
            Map.entry(JavaTokenType.MINUS, BinaryOperator.MINUS),
//            Map.entry(JavaTokenType.PLUSPLUS, Operator.PLUS_PLUS),
//            Map.entry(JavaTokenType.MINUSMINUS, Operator.MINUS_MINUS),
            Map.entry(JavaTokenType.EQEQ, BinaryOperator.EQ),
            Map.entry(JavaTokenType.NE, BinaryOperator.NE),
            Map.entry(JavaTokenType.GT, BinaryOperator.GT),
            Map.entry(JavaTokenType.GE, BinaryOperator.GE),
            Map.entry(JavaTokenType.LT, BinaryOperator.LT),
            Map.entry(JavaTokenType.LE, BinaryOperator.LE),
            Map.entry(JavaTokenType.ANDAND, BinaryOperator.AND),
            Map.entry(JavaTokenType.OROR, BinaryOperator.OR),
            Map.entry(JavaTokenType.ASTERISK, BinaryOperator.MULTIPLY),
            Map.entry(JavaTokenType.DIV, BinaryOperator.DIVIDE)
    );

    public static final Set<IElementType> BOOL_OPS = Set.of(
            JavaTokenType.ANDAND, JavaTokenType.OROR
    );

    private final MethodGenerator methodGenerator;
    private final TypeResolver typeResolver;
    private final VariableTable variableTable;
    private final ParameterizedFlowProvider parameterizedFlowProvider;
    private final ArrayTypeProvider arrayTypeProvider;
    private final Generator visitor;

    private final List<MethodCallResolver> methodCallResolvers = List.of(
            new ListAddResolver(), new ListRemoveResolver(), new ListGetResolver(),
            new ListIsEmptyResolver(), new ListSizeResolver(), new StringConcatResolver(),
            new ToStringResolver(), new ListClearResolver(), new ListAddAllResolver(),
            new GetPasswordResolver()
    );

    private final List<NewResolver> newResolvers = List.of(
            new NewListResolver(), new NewListWithInitialResolver(), new NewPasswordResolver()
    );

    public ExpressionResolver(MethodGenerator methodGenerator, VariableTable variableTable, TypeResolver typeResolver,
                              ArrayTypeProvider arrayTypeProvider,
                              ParameterizedFlowProvider parameterizedFlowProvider,
                              Generator visitor) {
        this.methodGenerator = methodGenerator;
        this.typeResolver = typeResolver;
        this.variableTable = variableTable;
        this.arrayTypeProvider = arrayTypeProvider;
        this.parameterizedFlowProvider = parameterizedFlowProvider;
        this.visitor = visitor;
    }

    public Expression resolve(PsiExpression psiExpression) {
        ResolutionContext context = new ResolutionContext();
        return resolve(psiExpression, context);
    }

    private Expression resolve(PsiExpression psiExpression, ResolutionContext context) {
        if (isBoolExpression(psiExpression)) {
            return resolveBoolExpr(psiExpression, context);
        } else {
            return resolveNormal(psiExpression, context);
        }
    }

//    public IEntityContext getEntityContext() {
//        return entityContext;
//    }

    public ArrayTypeProvider getArrayTypeProvider() {
        return arrayTypeProvider;
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
            case PsiInstanceOfExpression instanceOfExpression -> resolveInstanceOf(instanceOfExpression, context);
            case PsiLambdaExpression lambdaExpression -> resolveLambdaExpression(lambdaExpression, context);
            default -> throw new IllegalStateException("Unexpected value: " + psiExpression);
        };
    }

    private Expression resolveInstanceOf(PsiInstanceOfExpression instanceOfExpression, ResolutionContext context) {
        Expression operand;
        if (instanceOfExpression.getPattern() instanceof PsiTypeTestPattern pattern) {
            String varName = requireNonNull(pattern.getPatternVariable()).getName();
            operand = new NodeExpression(
                    methodGenerator.createValue(varName, resolve(instanceOfExpression.getOperand(), context))
            );
            variableTable.set(varName, operand);
        } else {
            operand = resolve(instanceOfExpression.getOperand(), context);
        }
        return new InstanceOfExpression(
                operand,
                typeResolver.resolveDeclaration(requireNonNull(instanceOfExpression.getCheckType()).getType())
        );
    }

    private Expression resolveArrayAccess(PsiArrayAccessExpression arrayAccessExpression, ResolutionContext context) {
        return new ArrayAccessExpression(
                resolve(arrayAccessExpression.getArrayExpression(), context),
                resolve(arrayAccessExpression.getIndexExpression(), context)
        );
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    private boolean isBoolExpression(PsiExpression psiExpression) {
        return psiExpression.getType() == PsiType.BOOLEAN;
    }

    private Expression resolveThis(PsiThisExpression ignored) {
        return variableTable.get("this");
    }

    private Expression resolveConditional(PsiConditionalExpression psiExpression, ResolutionContext context) {
        return new FunctionExpression(
                Func.IF,
                ArrayExpression.create(
                        List.of(
                                resolve(psiExpression.getCondition(), context),
                                resolve(requireNonNull(psiExpression.getThenExpression()), context),
                                resolve(requireNonNull(psiExpression.getElseExpression()), context)
                        ),
                        arrayTypeProvider
                )
        );
    }

    private Expression resolveParenthesized(PsiParenthesizedExpression psiExpression, ResolutionContext context) {
        return resolve(requireNonNull(psiExpression.getExpression()), context);
    }

    private Expression resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        if (value == null) {
            return new ConstantExpression(Instances.nullInstance());
        }
        Instance instance;
        PrimitiveType valueType = (PrimitiveType) typeResolver.resolve(literalExpression.getType());
        instance = switch (value) {
            case Boolean boolValue -> new BooleanInstance(boolValue, valueType);
            case Integer integer -> new LongInstance(integer, valueType);
            case Float floatValue -> new DoubleInstance(floatValue.doubleValue(), valueType);
            case Double doubleValue -> new DoubleInstance(doubleValue, valueType);
            case Long longValue -> new LongInstance(longValue, valueType);
            case String string -> Instances.stringInstance(string);
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
                return new FunctionExpression(Func.LEN, arrayExpr);
            } else {
                PsiClass psiClass = requireNonNull(psiField.getContainingClass());
                Field field;
                if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                    ClassType klass = (ClassType) typeResolver.resolveDeclaration(TranspileUtil.getRawType(psiClass));
                    field = Objects.requireNonNull(klass.findStaticFieldByCode(psiField.getName()));
                    return new StaticFieldExpression(field);
                } else {
                    var qualifierExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    ClassType klass = (ClassType) methodGenerator.getExpressionType(qualifierExpr);
                    typeResolver.ensureDeclared(klass);
                    field = klass.getFieldByCode(psiField.getName());
                    return new PropertyExpression(qualifierExpr, field);
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
                    BinaryOperator.EQ,
                    resolve(Objects.requireNonNull(psiPrefixExpression.getOperand()), context),
                    new ConstantExpression(Instances.booleanInstance(false))
            );
        }
        var resolvedOperand = resolve(requireNonNull(psiPrefixExpression.getOperand()), context);
        if (op == JavaTokenType.PLUSPLUS) {
            return processAssignment(
                    operand,
                    new BinaryExpression(
                            BinaryOperator.ADD,
                            resolvedOperand,
                            new ConstantExpression(Instances.longInstance(1))
                    ),
                    context
            );
        } else if (op == JavaTokenType.MINUSMINUS) {
            return processAssignment(
                    operand,
                    new BinaryExpression(
                            BinaryOperator.MINUS,
                            resolvedOperand,
                            new ConstantExpression(Instances.longInstance(1))
                    ),
                    context
            );
        } else {
            throw new InternalException("Unsupported prefix operator " + op);
        }
    }

    private Expression resolveUnary(PsiUnaryExpression psiUnaryExpression, ResolutionContext context) {
        if (psiUnaryExpression.getOperand() instanceof PsiLiteralExpression literalExpression) {
            return new UnaryExpression(
                    resolveUnaryOperator(psiUnaryExpression.getOperationSign().getTokenType()),
                    resolveLiteral(literalExpression)
            );
        }
        if (psiUnaryExpression instanceof PsiPrefixExpression prefixExpression) {
            return resolvePrefix(prefixExpression, context);
        }
        var op = psiUnaryExpression.getOperationSign().getTokenType();
        var resolvedOperand = resolve(requireNonNull(psiUnaryExpression.getOperand()), context);
        if (op == JavaTokenType.PLUSPLUS) {
            processAssignment(
                    (PsiReferenceExpression) psiUnaryExpression.getOperand(),
                    new BinaryExpression(
                            BinaryOperator.ADD,
                            resolvedOperand,
                            new ConstantExpression(Instances.longInstance(1))
                    ),
                    context
            );
            return resolvedOperand;
        } else if (op == JavaTokenType.MINUSMINUS) {
            processAssignment(
                    (PsiReferenceExpression) psiUnaryExpression.getOperand(),
                    new BinaryExpression(
                            BinaryOperator.MINUS,
                            resolvedOperand,
                            new ConstantExpression(Instances.longInstance(1))
                    ),
                    context
            );
            return resolvedOperand;
        } else {
            return new UnaryExpression(
                    resolveUnaryOperator(psiUnaryExpression.getOperationSign().getTokenType()),
                    resolve(Objects.requireNonNull(psiUnaryExpression.getOperand()), context)
            );
        }
    }

    private Expression resolveBinary(PsiBinaryExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationSign().getTokenType();
        if (BOOL_OPS.contains(op)) {
            return new BinaryExpression(
                    resolveOperator(psiExpression.getOperationSign()),
                    resolve(psiExpression.getLOperand(), context),
                    resolve(Objects.requireNonNull(psiExpression.getROperand()), context)
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
        var methodCallResolver = getMethodCallResolver(expression);
        if (methodCallResolver != null)
            return methodCallResolver.resolve(expression, this, methodGenerator);
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
                return Expressions.trueExpression();
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

    @Nullable
    private MethodCallResolver getMethodCallResolver(PsiMethodCallExpression methodCallExpression) {
        var methodExpr = methodCallExpression.getMethodExpression();
        var qualifier = methodExpr.getQualifierExpression();
        if (qualifier == null)
            return null;
        var method = (PsiMethod) Objects.requireNonNull(methodExpr.resolve());
        var signature = TranspileUtil.getSignature(method, (PsiClassType) qualifier.getType());
        return NncUtils.find(
                methodCallResolvers,
                resolver -> NncUtils.anyMatch(resolver.getSignatures(), s -> s.matches(signature))
        );
    }

    @Nullable
    private NewResolver getNewResolver(PsiNewExpression newExpression) {
        var method = (PsiMethod) newExpression.resolveConstructor();
        tech.metavm.autograph.MethodSignature signature;
        if (method != null)
            signature = TranspileUtil.getSignature(method, null);
        else {
            // For default constructor
            var rawType = (PsiClassType) TranspileUtil.getRawType(Objects.requireNonNull(newExpression.getType()));
            signature = tech.metavm.autograph.MethodSignature.create(rawType, rawType.getName());
        }
        return NncUtils.find(
                newResolvers,
                resolver -> NncUtils.anyMatch(resolver.getSignatures(), s -> s.matches(signature))
        );
    }

    private MethodCallNode createInvokeFlowNode(Expression self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
        List<Expression> arguments = NncUtils.map(argumentList.getExpressions(), arg -> resolve(arg, context));
        var exprType = (ClassType) methodGenerator.getExpressionType(self);
        return methodGenerator.createMethodCall(self, Objects.requireNonNull(exprType.findMethodByCode(flowCode)), arguments);
    }

    private Expression invokeFlow(Expression self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
        return createNodeExpression(createInvokeFlowNode(self, flowCode, argumentList, context));
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
        var rawMethod = (PsiMethod) Objects.requireNonNull(ref.resolve());
        PsiExpression psiSelf = (PsiExpression) ref.getQualifier();
        if (psiSelf != null) {
            if (rawMethod.getModifierList().hasModifierProperty(PsiModifier.STATIC)) {
                var psiClass = (PsiClass) ((PsiReferenceExpression) psiSelf).resolve();
                ensureTypeDeclared(TranspileUtil.createType(psiClass));
            } else {
                ensureTypeDeclared(NncUtils.requireNonNull(psiSelf.getType()));
            }
        }
        Expression qualifier = getQualifier(psiSelf, context);
        var selfType = Types.getClassType(methodGenerator.getExpressionType(qualifier));
        var method = resolveMethod(selfType, expression);
        List<Expression> args = NncUtils.map(
                expression.getArgumentList().getExpressions(),
                expr -> resolve(expr, context)
        );
        var node = methodGenerator.createMethodCall(qualifier, method, args);
        if (method.getReturnType().isVoid()) {
            return null;
        } else {
            return createNodeExpression(node);
        }
    }

    private Expression getQualifier(@Nullable PsiExpression qualifierExpression, ResolutionContext context) {
        return qualifierExpression == null ? variableTable.get("this")
                : resolve(requireNonNull(qualifierExpression), context);
    }

    private Method resolveMethod(ClassType declaringType, PsiCallExpression expression) {
        var methodGenerics = expression.resolveMethodGenerics();
        var substitutor = methodGenerics.getSubstitutor();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var psiParameters = NncUtils.requireNonNull(method.getParameterList()).getParameters();
        var template = declaringType.getEffectiveTemplate();
        List<Type> rawParamTypes = NncUtils.map(
                psiParameters,
                param -> typeResolver.resolveDeclaration(param.getType())
        );
        var templateMethod = template.getMethodByCodeAndParamTypes(method.getName(), rawParamTypes);
        Method piFlow = Objects.requireNonNull(template != declaringType ? declaringType.findMethodByVerticalTemplate(templateMethod) : templateMethod);
        Method flow;
        if (piFlow.getTypeParameters().isEmpty()) {
            flow = piFlow;
        } else {
            var flowTypeArgs = NncUtils.map(method.getTypeParameters(),
                    typeParam -> typeResolver.resolveDeclaration(substitutor.substitute(typeParam)));
            flow = parameterizedFlowProvider.getParameterizedFlow(piFlow, flowTypeArgs);
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
        var type = (ArrayType) typeResolver.resolveDeclaration(expression.getType());
        var node = methodGenerator.createNewArray(
                type,
                new ArrayExpression(
                        NncUtils.map(
                                expression.getArrayInitializer().getInitializers(),
                                this::resolve
                        ),
                        type
                )
        );
        return new NodeExpression(node);
    }

    private Expression resolveNewPojo(PsiNewExpression expression, ResolutionContext context) {
        var resolver = getNewResolver(expression);
        if (resolver != null)
            return resolver.resolve(expression, this, methodGenerator);
        var type = NncUtils.requireNonNull(expression.getType());
//        var psiClass = requireNonNull(((PsiClassType) type).resolve());
        var psiListType = TranspileUtil.createType(List.class);
        if (psiListType.isAssignableFrom(type)) {
            var listType = TranspileUtil.getSuperType(type, List.class);
            var mvElementType = typeResolver.resolve(listType.getParameters()[0]);
            var arrayType = arrayTypeProvider.getArrayType(mvElementType, ArrayKind.READ_WRITE);
            var node = methodGenerator.createNewArray(arrayType, null);
//            var listType = (ClassType) typeResolver.resolve(type);
//            var listType.getTypeArguments()[0];
//            var signature = resolveMethodSignature(expression);
//            var subFlow = listType.getFlowByCodeAndParamTypes("List", signature.parameterTypes);
//            var newNode = flowGenerator.createNew(subFlow,
//                    resolveExpressionList(requireNonNull(expression.getArgumentList()), context),
//                    true);
            return new NodeExpression(node);
        }
        var psiMapType = TranspileUtil.createType(Map.class);
        if (psiMapType.isAssignableFrom(type)) {
            var mapType = (ClassType) typeResolver.resolve(type);
            var subFlow = Objects.requireNonNull(mapType.findMethodByCode("Map"));
            var newNode = methodGenerator.createNew(subFlow,
                    resolveExpressionList(requireNonNull(expression.getArgumentList()), context),
                    true);
            return createNodeExpression(newNode);
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

    private record MethodSignature(String name, List<Type> parameterTypes, @Nullable Type returnType) {
    }

    private MethodSignature resolveMethodSignature(PsiCallExpression callExpression) {
        var methodGenerics = callExpression.resolveMethodGenerics();
        var substitutor = methodGenerics.getSubstitutor();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var psiParamTypes = NncUtils.map(
                requireNonNull(method.getParameterList()).getParameters(),
                p -> substitutor.substitute(p.getType())
        );
        var psiReturnType = substitutor.substitute(method.getReturnType());
        return new MethodSignature(
                method.getName(),
                NncUtils.map(psiParamTypes, typeResolver::resolveDeclaration),
                NncUtils.get(psiReturnType, typeResolver::resolveDeclaration)
        );
    }

    private List<Expression> resolveExpressionList(PsiExpressionList expressionList, ResolutionContext context) {
        return NncUtils.map(expressionList.getExpressions(), expression -> resolve(expression, context));
    }

    public Expression newInstance(ClassType declaringType, List<Expression> arguments,
                                  List<PsiType> prefixTypes, PsiConstructorCall constructorCall) {
        var methodGenerics = constructorCall.resolveMethodGenerics();
        if (methodGenerics.getElement() == null) {
            var flow = declaringType.getDefaultConstructor();
            return new NodeExpression(methodGenerator.createNew(flow, arguments, false));
        } else {
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
            paramTypes = NncUtils.union(prefixParamTypes, paramTypes);
            var flow = declaringType.getMethodByCodeAndParamTypes(Types.getConstructorCode(declaringType), paramTypes);
            var newNode = methodGenerator.createNew(flow, arguments, false);
            return new NodeExpression(newNode);
        }
    }

    private Expression createNodeExpression(NodeRT node) {
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
                assignment = new BinaryExpression(BinaryOperator.ADD, resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.MINUSEQ) {
                assignment = new BinaryExpression(BinaryOperator.MINUS, resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.ASTERISKEQ) {
                assignment = new BinaryExpression(BinaryOperator.MULTIPLY, resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.DIVEQ) {
                assignment = new BinaryExpression(BinaryOperator.DIVIDE, resolvedLeft, resolvedRight);
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
                ClassType instanceType = (ClassType) methodGenerator.getExpressionType(self);
                typeResolver.ensureDeclared(instanceType);
                Field field = instanceType.getFieldByCode(psiField.getName());
                UpdateObjectNode node = methodGenerator.createUpdateObject(self);
                node.setUpdateField(field, UpdateOp.SET, Values.expression(assignment));
            } else {
                variableTable.set(
                        variable.getName(),
                        new NodeExpression(methodGenerator.createValue(variable.getName(), assignment))
                );
            }
        } else {
            throw new InternalException("Invalid assignment target " + target);
        }
        return assignment;
    }

    void processChildAssignment(Expression self, @Nullable Field field, Expression assignment) {
        NncUtils.requireTrue(field == null || field.isChild());
        NncUtils.requireTrue(assignment instanceof NodeExpression);
        var node = ((NodeExpression) assignment).getNode();
        if (node instanceof NewNode newNode) {
            newNode.setParentRef(new ParentRef(Values.expression(self), field));
        } else {
            throw new InternalException(
                    String.format("Only new objects are allowed to be assigned to a child. field: %s",
                            field)
            );
        }
    }

    private BinaryOperator resolveOperator(PsiJavaToken psiOp) {
        return resolveOperator(psiOp.getTokenType());
    }

    private UnaryOperator resolveUnaryOperator(IElementType elementType) {
        return UNARY_OPERATOR_MAP.get(elementType);
    }

    private BinaryOperator resolveOperator(IElementType elementType) {
        return OPERATOR_MAP.get(elementType);
    }

    public void constructBool(PsiExpression expression, BranchNode parent) {
        constructBool(expression, false, parent, new ResolutionContext());
    }

    public Expression resolveBoolExpr(PsiExpression expression, ResolutionContext context) {
        BranchNode branchNode = methodGenerator.createBranchNode(false);
        var thenBranch = branchNode.addBranch(Values.expression(Expressions.trueExpression()));
        var elseBranch = branchNode.addDefaultBranch();

        methodGenerator.enterCondSection(branchNode);
        methodGenerator.enterBranch(thenBranch);
        constructBool(expression, false, branchNode, new ResolutionContext());
        methodGenerator.exitBranch();
        var mergeNode = methodGenerator.createMerge();
        methodGenerator.exitCondSection(mergeNode, List.of());

        var valueField = FieldBuilder
                .newBuilder("value", "value", mergeNode.getType(), StandardTypes.getBooleanType())
                .build();
        new MergeNodeField(valueField, mergeNode, Map.of(
                thenBranch, Values.expression(Expressions.trueExpression()),
                elseBranch, Values.expression(Expressions.falseExpression())
        ));
        return new PropertyExpression(new NodeExpression(mergeNode), valueField);
    }

    public void constructBool(PsiExpression expression, boolean negated, BranchNode parent, ResolutionContext context) {
        switch (expression) {
            case PsiBinaryExpression binaryExpression ->
                    constructBinaryBool(binaryExpression, negated, parent, context);
            case PsiUnaryExpression unaryExpression -> constructUnaryBool(unaryExpression, negated, parent, context);
            case PsiPolyadicExpression polyadicExpression ->
                    constructPolyadicBool(polyadicExpression, negated, parent, context);
            default -> constructAtomicBool(expression, negated, parent, context);
        }
    }

    private void constructUnaryBool(PsiUnaryExpression unaryExpression, boolean negated, BranchNode parent, ResolutionContext context) {
        var op = unaryExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EXCL) {
            constructBool(requireNonNull(unaryExpression.getOperand()), !negated, parent, context);
        } else {
            throw new InternalException("Invalid unary operator for bool expression: " + op);
        }
    }

    public Expression resolveLambdaExpression(PsiLambdaExpression expression, ResolutionContext context) {
        var returnType = typeResolver.resolveDeclaration(TranspileUtil.getLambdaReturnType(expression));
        var parameters = resolveParameterList(expression.getParameterList());
        var funcInterface = (ClassType) typeResolver.resolveDeclaration(expression.getFunctionalInterfaceType());
        var lambdaNode = methodGenerator.createLambda(parameters, returnType, funcInterface);
        methodGenerator.enterScope(lambdaNode.getBodyScope());
        var inputNode = methodGenerator.createInput();
        for (Parameter parameter : parameters) {
            var field = FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputNode.getType(), parameter.getType())
                    .build();
            methodGenerator.setVariable(parameter.getCode(),
                    new PropertyExpression(new NodeExpression(inputNode), field));
        }
        if (expression.getBody() instanceof PsiExpression bodyExpr) {
            methodGenerator.createReturn(resolve(bodyExpr, context));
        } else {
            requireNonNull(expression.getBody()).accept(visitor);
        }
        methodGenerator.exitScope();
        return new NodeExpression(lambdaNode);
    }

    private List<Parameter> resolveParameterList(PsiParameterList parameterList) {
        return NncUtils.map(parameterList.getParameters(), this::resolveParameter);
    }

    private Parameter resolveParameter(PsiParameter psiParameter) {
        return new Parameter(
                null, TranspileUtil.getFlowParamName(psiParameter), psiParameter.getName(),
                typeResolver.resolveDeclaration(psiParameter.getType())
        );
    }

    public void constructBinaryBool(PsiBinaryExpression psiBinaryExpression, boolean negated, BranchNode parent, ResolutionContext context) {
        var op = resolveOperator(psiBinaryExpression.getOperationSign());
        var first = NncUtils.requireNonNull(psiBinaryExpression.getLOperand());
        var second = NncUtils.requireNonNull(psiBinaryExpression.getROperand());
        if (negated) {
            op = op.complement();
        }
        if (op == BinaryOperator.AND) {
            constructBool(first, negated, parent, context);
            constructBool(second, negated, parent, context);
        } else if (op == BinaryOperator.OR) {
            var branchNode = methodGenerator.createBranchNode(false);
//            var branch1 = branchNode.addBranch(Value.expression(ExpressionUtil.trueExpression()));
//            branchNode.addDefaultBranch();

            methodGenerator.enterCondSection(branchNode);

            methodGenerator.enterBranch(branchNode.addBranch(trueValue()));
            constructBool(first, negated, branchNode, context);
            methodGenerator.exitBranch();

            methodGenerator.enterBranch(branchNode.addBranch(trueValue()));
            constructBool(second, negated, branchNode, context);
            methodGenerator.exitBranch();

            methodGenerator.enterBranch(branchNode.addDefaultBranch());
            methodGenerator.createCheck(Expressions.falseExpression(), parent);
            methodGenerator.exitBranch();

//            flowBuilder.enterScope(branch1.getScope());
//            constructBool(first, !negated, branchNode, context);
//            constructBool(second, !negated, branchNode, context);
//            flowBuilder.createCheck(ExpressionUtil.falseExpression(), parent);
//            flowBuilder.exitScope();
//            flowBuilder.exitBranch();
            methodGenerator.exitCondSection(methodGenerator.createMerge(), List.of());
        } else {
            var expr = resolveBinary(psiBinaryExpression, context);
            methodGenerator.createCheck(expr, parent);
        }
    }

    private Value trueValue() {
        return Values.constant(Expressions.trueExpression());
    }

    private Value falseValue() {
        return Values.constant(Expressions.falseExpression());
    }

    public void constructPolyadicBool(PsiPolyadicExpression expression, boolean negated, BranchNode parent, ResolutionContext context) {
        var op = resolveOperator(expression.getOperationTokenType());
        if (negated) {
            op = op.complement();
        }
        if (op == BinaryOperator.AND) {
            for (PsiExpression operand : expression.getOperands()) {
                constructBool(operand, negated, parent, context);
            }
        } else if (op == BinaryOperator.OR) {
            var branchNode = methodGenerator.createBranchNode(false);
            var thenBranch = branchNode.addBranch(Values.expression(Expressions.trueExpression()));
            branchNode.addDefaultBranch();
            methodGenerator.enterScope(thenBranch.getScope());
            var operands = expression.getOperands();
            for (PsiExpression operand : operands) {
                constructBool(operand, !negated, branchNode, context);
            }
            methodGenerator.createCheck(Expressions.falseExpression(), parent);
            methodGenerator.exitScope();
            methodGenerator.createMerge();
        } else {
            throw new InternalException("Invalid operator for polyadic boolean expression: " + op);
        }
    }

    private Expression constructAtomicBool(PsiExpression expression, boolean negated, BranchNode parent, ResolutionContext context) {
        var expr = resolveNormal(expression, context);
        if (negated) {
            expr = Expressions.not(expr);
        }
        methodGenerator.createCheck(expr, parent);
        return expr;
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

        public void finish(VariableTable variableTable, MethodGenerator flowBuilder) {
            for (VariableOperation(String variableName, Expression value) : variableOperations) {
                var node = flowBuilder.createValue(variableName, value);
                variableTable.set(variableName, new NodeExpression(node));
            }
            for (FieldOperation(Expression instance, Field field, Expression value) : fieldOperations) {
                var node = flowBuilder.createUpdateObject(instance);
                node.setUpdateField(field, UpdateOp.SET, Values.expression(value));
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
