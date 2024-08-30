package org.metavm.autograph;

import com.google.common.collect.Streams;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.DoubleValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.CompilerException;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;

import static java.util.Objects.requireNonNull;
import static org.metavm.autograph.TranspileUtils.matchClass;
import static org.metavm.autograph.TranspileUtils.matchMethod;
import static org.metavm.util.ReflectionUtils.getMethod;

public class ExpressionResolver {

    public static final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    public static final Map<IElementType, UnaryOperator> UNARY_OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.PLUS, UnaryOperator.POS),
            Map.entry(JavaTokenType.MINUS, UnaryOperator.NEG),
            Map.entry(JavaTokenType.EXCL, UnaryOperator.NOT)
    );

    private static final Map<IElementType, BinaryOperator> OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.PLUS, BinaryOperator.ADD),
            Map.entry(JavaTokenType.MINUS, BinaryOperator.MINUS),
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
    private final VisitorBase visitor;
    private final Map<PsiExpression, Expression> expressionMap = new IdentityHashMap<>();

    private final List<MethodCallResolver> methodCallResolvers = List.of(
            new ListOfResolver(), new SetOfResolver(), new IndexUtilsCallResolver()
    );

    private final List<NewResolver> newResolvers = List.of(
            new NewPasswordResolver(), new NewDateResolver()
    );

    public ExpressionResolver(MethodGenerator methodGenerator, VariableTable variableTable, TypeResolver typeResolver, VisitorBase visitor) {
        this.methodGenerator = methodGenerator;
        this.typeResolver = typeResolver;
        this.variableTable = variableTable;
        this.visitor = visitor;
    }

    public Expression resolve(PsiExpression psiExpression) {
        ResolutionContext context = new ResolutionContext();
        try {
            return resolve(psiExpression, context);
        }
        catch (Exception e) {
            throw new InternalException("Failed to resolve expression: " + psiExpression.getText(), e);
        }
    }

    private Expression resolve(PsiExpression psiExpression, ResolutionContext context) {
        Expression resolved;
        if (isBoolExpression(psiExpression)) {
            resolved = resolveBoolExpr(psiExpression, context);
        } else {
            resolved = resolveNormal(psiExpression, context);
        }
//        if(resolved != null && resolved.getType().isCaptured())
//            TranspileUtil.forEachCapturedTypePairs(psiExpression.getType(), resolved.getType(), typeResolver::mapCapturedType);
        return resolved;
    }

    private CapturedType resolveCapturedType(PsiCapturedWildcardType psiCapturedWildcardType) {
        return (CapturedType) expressionMap.get((PsiExpression) psiCapturedWildcardType.getContext()).getType();
    }

    private Expression resolveNormal(PsiExpression psiExpression, ResolutionContext context) {
        return switch (psiExpression) {
            case PsiBinaryExpression binaryExpression -> resolveBinary(binaryExpression, context);
            case PsiPolyadicExpression polyadicExpression -> resolvePolyadic(polyadicExpression, context);
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
            case PsiTypeCastExpression typeCastExpression -> resolveTypeCast(typeCastExpression, context);
            case PsiSwitchExpression switchExpression -> resolveSwitchExpression(switchExpression, context);
            default -> throw new IllegalStateException("Unexpected value: " + psiExpression);
        };
    }

    private Expression resolveTypeCast(PsiTypeCastExpression typeCastExpression, ResolutionContext context) {
        var operand = resolve(typeCastExpression.getOperand(), context);
        var targetType = typeResolver.resolveDeclaration(requireNonNull(typeCastExpression.getCastType()).getType());
        return Expressions.node(methodGenerator.createTypeCast(operand, targetType));
    }

    private Expression resolvePolyadic(PsiPolyadicExpression psiExpression, ResolutionContext context) {
        var operator = resolveOperator(psiExpression.getOperationTokenType());
        var operands = NncUtils.map(psiExpression.getOperands(), expr -> resolve(expr, context));
        var current = operands.get(0);
        for (int i = 1; i < operands.size(); i++) {
            current = new BinaryExpression(operator, current, operands.get(i));
        }
        return current;
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
        var condition = resolve(psiExpression.getCondition(), context);
        var branchNode = methodGenerator.createBranchNode(false);
        var thenBranch = branchNode.addBranch(Values.expression(condition));
        var elseBranch = branchNode.addDefaultBranch();
        methodGenerator.enterCondSection(branchNode);
        methodGenerator.enterBranch(thenBranch);
        var thenExpr = resolve(psiExpression.getThenExpression(), context);
        methodGenerator.exitBranch();

        methodGenerator.enterBranch(elseBranch);
        var elseExpr = resolve(psiExpression.getElseExpression(), context);
        methodGenerator.exitBranch();

        var mergeNode = methodGenerator.createMerge();
        methodGenerator.exitCondSection(mergeNode, List.of());

        var valueField = FieldBuilder
                .newBuilder("value", "value", mergeNode.getKlass(),
                        Types.getUnionType(List.of(thenExpr.getType(), elseExpr.getType())))
                .build();
        new MergeNodeField(valueField, mergeNode, Map.of(
                thenBranch, Values.expression(thenExpr),
                elseBranch, Values.expression(elseExpr))
        );
        return new PropertyExpression(new NodeExpression(mergeNode), valueField.getRef());
    }

    private Expression resolveParenthesized(PsiParenthesizedExpression psiExpression, ResolutionContext context) {
        return resolve(requireNonNull(psiExpression.getExpression()), context);
    }

    private Expression resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        if (value == null) {
            return new ConstantExpression(Instances.nullInstance());
        }
        Value instance;
        PrimitiveType valueType = (PrimitiveType) typeResolver.resolve(literalExpression.getType());
        instance = switch (value) {
            case Boolean boolValue -> new BooleanValue(boolValue, valueType);
            case Integer integer -> new LongValue(integer, valueType);
            case Float floatValue -> new DoubleValue(floatValue.doubleValue(), valueType);
            case Double doubleValue -> new DoubleValue(doubleValue, valueType);
            case Long longValue -> new LongValue(longValue, valueType);
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
                    var klass = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass))).resolve();
                    field = Objects.requireNonNull(klass.findStaticFieldByCode(psiField.getName()));
                    return new StaticPropertyExpression(field.getRef());
                } else {
                    var qualifierExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    Klass klass = Types.resolveKlass(methodGenerator.getExpressionType(qualifierExpr));
                    typeResolver.ensureDeclared(klass);
                    field = klass.getFieldByCode(psiField.getName());
                    return new PropertyExpression(qualifierExpr, field.getRef());
                }
            }
        } else if (target instanceof PsiMethod psiMethod) {
            PsiClass psiClass = requireNonNull(psiMethod.getContainingClass());
            Method method;
            if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                Klass klass = Types.resolveKlass(typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass)));
                method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                return new StaticPropertyExpression(method.getRef());
            } else {
                if (psiReferenceExpression.getQualifierExpression() instanceof PsiReferenceExpression refExpr &&
                        refExpr.resolve() instanceof PsiClass) {
                    var klass = Types.resolveKlass(typeResolver.resolve(TranspileUtils.createType(psiClass)));
                    method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                    return new StaticPropertyExpression(method.getRef());
                } else {
                    var qualifierExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    Klass klass = Types.resolveKlass(methodGenerator.getExpressionType(qualifierExpr));
                    typeResolver.ensureDeclared(klass);
                    method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                    return new PropertyExpression(qualifierExpr, method.getRef());
                }
            }
        } else if (target instanceof PsiVariable variable) {
            return variableTable.get(variable.getName());
        } else {
            throw new InternalException("Can not resolve reference expression with target: " + target);
        }
    }

    private boolean isClassRef(PsiExpression psiExpression) {
        return psiExpression instanceof PsiReferenceExpression refExpr &&
                refExpr.resolve() instanceof PsiClass;
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
        return resolveFlowCall(expression, context);
    }

    @Nullable
    private MethodCallResolver getMethodCallResolver(PsiMethodCallExpression methodCallExpression) {
        var methodExpr = methodCallExpression.getMethodExpression();
        var qualifier = methodExpr.getQualifierExpression();
        var method = (PsiMethod) Objects.requireNonNull(methodExpr.resolve());
        var signature = TranspileUtils.getSignature(method, NncUtils.get(qualifier, q -> (PsiClassType) q.getType()));
        return Streams.concat(
                        TranspileUtils.getNativeFunctionCallResolvers().stream(),
                        methodCallResolvers.stream()
                )
                .filter(resolver -> NncUtils.anyMatch(resolver.getSignatures(), s -> s.matches(signature)))
                .findFirst().orElse(null);
    }

    @Nullable
    private NewResolver getNewResolver(PsiNewExpression newExpression) {
        var method = (PsiMethod) newExpression.resolveConstructor();
        org.metavm.autograph.MethodSignature signature;
        if (method != null)
            signature = TranspileUtils.getSignature(method, null);
        else {
            // For default constructor
            var rawType = (PsiClassType) TranspileUtils.getRawType(Objects.requireNonNull(newExpression.getType()));
            signature = org.metavm.autograph.MethodSignature.create(rawType, rawType.getName());
        }
        return NncUtils.find(
                newResolvers,
                resolver -> NncUtils.anyMatch(resolver.getSignatures(), s -> s.matches(signature))
        );
    }

    private MethodCallNode createInvokeFlowNode(Expression self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
        List<Expression> arguments = NncUtils.map(argumentList.getExpressions(), arg -> resolve(arg, context));
        var exprType = Types.resolveKlass(methodGenerator.getExpressionType(self));
        typeResolver.ensureDeclared(exprType);
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
        var klassName = requireNonNull(requireNonNull(rawMethod.getContainingClass()).getQualifiedName());
        if (klassName.startsWith("org.metavm.lang.")) {
            throw new InternalException("Native method should be resolved by MethodResolver: " + klassName + "." + rawMethod.getName());
        }
        var isStatic = TranspileUtils.isStatic(rawMethod);
        PsiExpression psiSelf = (PsiExpression) ref.getQualifier();
        var qualifier = isStatic ? null : getQualifier(psiSelf, context);
        if (psiSelf != null) {
            if (isStatic) {
                var psiClass = (PsiClass) ((PsiReferenceExpression) psiSelf).resolve();
                ensureTypeDeclared(TranspileUtils.createType(psiClass));
            } else {
                ensureTypeDeclared(NncUtils.requireNonNull(psiSelf.getType()));
            }
        }
        var substitutor = expression.resolveMethodGenerics().getSubstitutor();

        var psiArgs = expression.getArgumentList().getExpressions();
        var args = new ArrayList<Expression>();
        for (PsiExpression psiArg : psiArgs) {
            var arg = resolve(psiArg, context);
            var paramType = typeResolver.resolveDeclaration(substitutor.substitute(psiArg.getType()));
            if (paramType instanceof ClassType classType) {
                var klass = classType.resolve();
                if (klass.isSAMInterface()
                        && arg.getType() instanceof FunctionType) {
                    arg = new NodeExpression(createSAMConversion(klass, arg));
                }
            }
            args.add(arg);
        }
        var method = resolveMethod(expression);
        var node = methodGenerator.createMethodCall(qualifier, method, args);
        setCapturedExpressions(node, context);
        if (method.getReturnType().isVoid()) {
            return null;
        } else {
            return createNodeExpression(node);
        }
    }

    private void setCapturedExpressions(CallNode node, ResolutionContext context) {
        var flow = node.getFlowRef();
        var capturedTypeSet = new HashSet<CapturedType>();
        if (flow instanceof MethodRef methodRef)
            methodRef.getDeclaringType().getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        flow.getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        var capturedTypes = new ArrayList<>(capturedTypeSet);
        var psiCapturedTypes = NncUtils.map(capturedTypes, typeResolver::getPsiCapturedType);
        var capturedPsiExpressions = NncUtils.map(psiCapturedTypes, t -> (PsiExpression) t.getContext());
        var capturedExpressions = NncUtils.map(capturedPsiExpressions, e -> resolve(e, context));
        var captureExpressionTypes = NncUtils.map(
                capturedPsiExpressions, e -> typeResolver.resolveDeclaration(e.getType()));
        node.setCapturedExpressions(capturedExpressions);
        node.setCapturedExpressionTypes(captureExpressionTypes);
    }

    private NodeRT createSAMConversion(Klass samInterface, Expression function) {
        var func = StdFunction.functionToInstance.get().getParameterized(List.of(samInterface.getType()));
        return methodGenerator.createFunctionCall(func, List.of(function));
    }

    private Expression getQualifier(@Nullable PsiExpression qualifierExpression, ResolutionContext context) {
        return qualifierExpression == null ? variableTable.get("this")
                : resolve(requireNonNull(qualifierExpression), context);
    }

    private Method resolveMethod(PsiCallExpression expression) {
        var methodGenerics = expression.resolveMethodGenerics();
        var substitutor = methodGenerics.getSubstitutor();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var declaringType = Types.resolveKlass(typeResolver.resolveDeclaration(
                substitutor.substitute(TranspileUtils.createTemplateType(requireNonNull(method.getContainingClass())))
        ));
        var psiParameters = NncUtils.requireNonNull(method.getParameterList()).getParameters();
        var template = declaringType.getEffectiveTemplate();
        List<Type> rawParamTypes = NncUtils.map(
                psiParameters,
                param -> {
                    var t = typeResolver.resolveDeclaration(param.getType());
                    if (TranspileUtils.getAnnotation(param, Nullable.class) != null)
                        t = new UnionType(Set.of(t, Types.getNullType()));
                    return t;
                }
        );
        var templateMethod = template.getMethodByCodeAndParamTypes(method.getName(), rawParamTypes).getEffectiveVerticalTemplate();
        Method piFlow = Objects.requireNonNull(template != declaringType ? declaringType.findMethodByVerticalTemplate(templateMethod) : templateMethod);
        Method flow;
        if (piFlow.getTypeParameters().isEmpty()) {
            flow = piFlow;
        } else {
            var flowTypeArgs = NncUtils.map(method.getTypeParameters(),
                    typeParam -> typeResolver.resolveDeclaration(substitutor.substitute(typeParam)));
            flow = piFlow.getParameterized(flowTypeArgs);
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
        if (expression.getType() instanceof PsiClassType psiClassType) {
            var klass = Types.resolveKlass(typeResolver.resolve(psiClassType));
            List<Expression> args = NncUtils.map(
                    requireNonNull(expression.getArgumentList()).getExpressions(),
                    expr -> resolve(expr, context)
            );
            return newInstance(klass, args, List.of(), expression, context);
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

    public Expression newInstance(Klass declaringType, List<Expression> arguments,
                                  List<PsiType> prefixTypes, PsiConstructorCall constructorCall, ResolutionContext context) {
        var methodGenerics = constructorCall.resolveMethodGenerics();
        NewObjectNode node;
        if (methodGenerics.getElement() == null) {
            var flow = declaringType.getDefaultConstructor();
            node = methodGenerator.createNew(flow, arguments, false, false);

        } else {
            var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
            var substitutor = methodGenerics.getSubstitutor();
            var prefixParamTypes = NncUtils.map(
                    prefixTypes,
                    type -> typeResolver.resolveTypeOnly(substitutor.substitute(type))
            );
            var paramTypes = NncUtils.map(
                    requireNonNull(method.getParameterList()).getParameters(),
                    param -> resolveParameterType(param, substitutor)
            );
            paramTypes = NncUtils.union(prefixParamTypes, paramTypes);
            var flow = declaringType.getMethodByCodeAndParamTypes(Types.getConstructorCode(declaringType), paramTypes);
            node = methodGenerator.createNew(flow, arguments, false, false);
        }
        setCapturedExpressions(node, context);
        return new NodeExpression(node);
    }

    private Type resolveParameterType(PsiParameter param, PsiSubstitutor substitutor) {
        var t = typeResolver.resolveTypeOnly(substitutor.substitute(param.getType()));
        if (TranspileUtils.getAnnotation(param, Nullable.class) != null)
            t = new UnionType(Set.of(t, Types.getNullType()));
        return t;
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
                if(TranspileUtils.isStatic(psiField)) {
                    var klass =  ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.createType(psiField.getContainingClass()))).resolve();
                    var field = klass.getStaticFieldByName(psiField.getName());
                    methodGenerator.createUpdateStatic(klass, Map.of(field,  assignment));
                }
                else {
                    Expression self;
                    if (assigned.getQualifierExpression() != null) {
                        self = resolve(assigned.getQualifierExpression(), context);
                    } else {
                        self = variableTable.get("this");
                    }
                    Klass instanceType = Types.resolveKlass(methodGenerator.getExpressionType(self));
                    typeResolver.ensureDeclared(instanceType);
                    Field field = instanceType.getFieldByCode(psiField.getName());
                    UpdateObjectNode node = methodGenerator.createUpdateObject(self);
                    node.setUpdateField(field, UpdateOp.SET, Values.expression(assignment));
                }
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
            newNode.setParentRef(new ParentRef(Values.expression(self), NncUtils.get(field, Field::getRef)));
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
                .newBuilder("value", "value", mergeNode.getKlass(), Types.getBooleanType())
                .build();
        new MergeNodeField(valueField, mergeNode, Map.of(
                thenBranch, Values.expression(Expressions.trueExpression()),
                elseBranch, Values.expression(Expressions.falseExpression())
        ));
        return new PropertyExpression(new NodeExpression(mergeNode), valueField.getRef());
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
        var returnType = typeResolver.resolveDeclaration(TranspileUtils.getLambdaReturnType(expression));
        var parameters = resolveParameterList(expression.getParameterList());
        var funcInterface = Types.resolveKlass(typeResolver.resolveDeclaration(expression.getFunctionalInterfaceType()));
        var lambdaNode = methodGenerator.createLambda(parameters, returnType, funcInterface);
        methodGenerator.enterScope(lambdaNode.getBodyScope());
        var inputNode = methodGenerator.createInput();
        for (Parameter parameter : parameters) {
            var field = FieldBuilder.newBuilder(parameter.getName(), parameter.getCode(), inputNode.getKlass(), parameter.getType())
                    .build();
            methodGenerator.setVariable(parameter.getCode(),
                    new PropertyExpression(new NodeExpression(inputNode), field.getRef()));
        }
        if (expression.getBody() instanceof PsiExpression bodyExpr) {
            methodGenerator.createReturn(resolve(bodyExpr, context));
        } else {
            requireNonNull(expression.getBody()).accept(visitor);
            if (lambdaNode.getReturnType().isVoid()) {
                var lastNode = lambdaNode.getBodyScope().getLastNode();
                if (lastNode == null || !lastNode.isExit())
                    methodGenerator.createReturn();
            }
        }
        methodGenerator.exitScope();
        return new NodeExpression(lambdaNode);
    }


    private Expression resolveSwitchExpression(PsiSwitchExpression psiSwitchExpression, ResolutionContext context) {
        var switchExpr = resolve(psiSwitchExpression.getExpression(), context);
        var branchNode = methodGenerator.createBranchNode(false);
        methodGenerator.enterCondSection(branchNode);
        var body = requireNonNull(psiSwitchExpression.getBody());
        var statements = requireNonNull(body.getStatements());
        for (PsiStatement statement : statements) {
            if (statement instanceof PsiSwitchLabeledRuleStatement labeledRuleStatement) {
                var branch = labeledRuleStatement.isDefaultCase() ?
                        branchNode.addDefaultBranch() :
                        branchNode.addBranch(Values.expression(resolveSwitchCaseCondition(switchExpr, labeledRuleStatement, context)));
                methodGenerator.enterBranch(branch);
                var caseBody = requireNonNull(labeledRuleStatement.getBody());
                if (caseBody instanceof PsiExpressionStatement exprStmt) {
                    methodGenerator.setYield(resolve(exprStmt.getExpression(), context));
                } else {
                    caseBody.accept(visitor);
                }
                methodGenerator.exitBranch();
            }
        }
        var mergeNode = methodGenerator.createMerge();
        methodGenerator.exitCondSection(mergeNode, List.of(), true);
        return Expressions.nodeProperty(mergeNode, mergeNode.getKlass().getFieldByCode("yield"));
    }

    private Expression resolveSwitchCaseCondition(Expression switchExpression, PsiSwitchLabeledRuleStatement switchLabeledRuleStatement, ResolutionContext context) {
        if (switchLabeledRuleStatement.isDefaultCase())
            return Expressions.trueExpression();
        var caseElementList = requireNonNull(switchLabeledRuleStatement.getCaseLabelElementList());
        Expression cond = null;
        for (PsiCaseLabelElement element : caseElementList.getElements()) {
            var newCond = Expressions.eq(switchExpression, resolve((PsiExpression) element, context));
            if (cond == null)
                cond = newCond;
            else
                cond = Expressions.or(cond, newCond);
        }
        return cond;
    }

    private List<Parameter> resolveParameterList(PsiParameterList parameterList) {
        return NncUtils.map(parameterList.getParameters(), this::resolveParameter);
    }

    private Parameter resolveParameter(PsiParameter psiParameter) {
        return new Parameter(
                null, TranspileUtils.getFlowParamName(psiParameter), psiParameter.getName(),
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

    private org.metavm.flow.Value trueValue() {
        return Values.constant(Expressions.trueExpression());
    }

    private org.metavm.flow.Value falseValue() {
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
