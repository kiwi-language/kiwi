package org.metavm.autograph;

import com.google.common.collect.Streams;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.BinaryOperator;
import org.metavm.flow.*;
import org.metavm.object.instance.core.BooleanValue;
import org.metavm.object.instance.core.DoubleValue;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class ExpressionResolver {

    public static final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    private static final Map<IElementType, BinaryOperator> OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.ASTERISK, BinaryOperator.MUL),
            Map.entry(JavaTokenType.DIV, BinaryOperator.DIV),
            Map.entry(JavaTokenType.PLUS, BinaryOperator.ADD),
            Map.entry(JavaTokenType.MINUS, BinaryOperator.SUB),
            Map.entry(JavaTokenType.GTGT, BinaryOperator.RIGHT_SHIFT),
            Map.entry(JavaTokenType.GTGTGT, BinaryOperator.UNSIGNED_RIGHT_SHIFT),
            Map.entry(JavaTokenType.LTLT, BinaryOperator.LEFT_SHIFT),
            Map.entry(JavaTokenType.EQEQ, BinaryOperator.EQ),
            Map.entry(JavaTokenType.NE, BinaryOperator.NE),
            Map.entry(JavaTokenType.GT, BinaryOperator.GT),
            Map.entry(JavaTokenType.GE, BinaryOperator.GE),
            Map.entry(JavaTokenType.LT, BinaryOperator.LT),
            Map.entry(JavaTokenType.LE, BinaryOperator.LE),
            Map.entry(JavaTokenType.AND, BinaryOperator.BITWISE_AND),
            Map.entry(JavaTokenType.XOR, BinaryOperator.BITWISE_XOR),
            Map.entry(JavaTokenType.OR, BinaryOperator.BITWISE_OR),
            Map.entry(JavaTokenType.ANDAND, BinaryOperator.AND),
            Map.entry(JavaTokenType.OROR, BinaryOperator.OR),
            Map.entry(JavaTokenType.PERC, BinaryOperator.MOD)
    );

    private final MethodGenerator methodGenerator;
    private final TypeResolver typeResolver;
    private final VisitorBase visitor;
    private final Map<PsiExpression, NodeRT> expression2node = new IdentityHashMap<>();
    private final LinkedList<PsiLambdaExpression> lambdas = new LinkedList<>();

    private final List<MethodCallResolver> methodCallResolvers = List.of(
            new ListOfResolver(), new SetOfResolver(), new IndexUtilsCallResolver()
    );

    private final List<NewResolver> newResolvers = List.of(
            new NewPasswordResolver(), new NewDateResolver()
    );

    public ExpressionResolver(MethodGenerator methodGenerator, TypeResolver typeResolver, VisitorBase visitor) {
        this.methodGenerator = methodGenerator;
        this.typeResolver = typeResolver;
        this.visitor = visitor;
    }

    public NodeRT resolve(PsiExpression psiExpression) {
        ResolutionContext context = new ResolutionContext();
        try {
            return resolve(psiExpression, context);
        }
        catch (Exception e) {
            throw new InternalException("Failed to resolve expression: " + psiExpression.getClass().getSimpleName() + " " + psiExpression.getText(), e);
        }
    }

    private NodeRT resolve(PsiExpression psiExpression, ResolutionContext context) {
        NodeRT resolved;
        if (isBoolExpression(psiExpression)) {
            resolved = resolveBoolExpr(psiExpression, context);
        } else {
            resolved = resolveNormal(psiExpression, context);
        }
        if(resolved != null && !expression2node.containsKey(psiExpression))
            expression2node.put(psiExpression, resolved);
        return resolved;
    }

    private NodeRT resolveNormal(PsiExpression psiExpression, ResolutionContext context) {
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
            case PsiSuperExpression superExpression -> resolveSuper(superExpression);
            case PsiParenthesizedExpression parExpression -> resolveParenthesized(parExpression, context);
            case PsiConditionalExpression conditionalExpression -> resolveConditional(conditionalExpression, context);
            case PsiArrayAccessExpression arrayAccessExpression -> resolveArrayAccess(arrayAccessExpression, context);
            case PsiInstanceOfExpression instanceOfExpression -> resolveInstanceOf(instanceOfExpression, context);
            case PsiLambdaExpression lambdaExpression -> resolveLambdaExpression(lambdaExpression, context);
            case PsiTypeCastExpression typeCastExpression -> resolveTypeCast(typeCastExpression, context);
            case PsiSwitchExpression switchExpression -> resolveSwitchExpression(switchExpression, context);
            case PsiClassObjectAccessExpression classObjectAccessExpression -> resolveClassObjectAccess(classObjectAccessExpression, context);
            case PsiArrayInitializerExpression arrayInitializerExpression -> resolveArrayInitialization(arrayInitializerExpression, context);
            default -> throw new IllegalStateException("Unexpected value: " + psiExpression);
        };
    }

    private NodeRT resolveSuper(PsiSuperExpression ignored) {
        return loadThis();
    }

    private NodeRT resolveClassObjectAccess(PsiClassObjectAccessExpression classObjectAccessExpression, ResolutionContext ignored) {
        var type = typeResolver.resolveTypeOnly(classObjectAccessExpression.getOperand().getType());
        return methodGenerator.createLoadType(type);
    }

    private NodeRT resolveTypeCast(PsiTypeCastExpression typeCastExpression, ResolutionContext context) {
        resolve(typeCastExpression.getOperand(), context);
        var targetType = typeResolver.resolveDeclaration(requireNonNull(typeCastExpression.getCastType()).getType());
        return methodGenerator.createTypeCast(targetType);
    }

    private NodeRT resolvePolyadic(PsiPolyadicExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationTokenType();
        var operands = psiExpression.getOperands();
        var current = resolve(operands[0], context);
        for (int i = 1; i < operands.length; i++) {
            resolve(operands[i]);
            current = resolveBinary(op);
        }
        return current;
    }

    private NodeRT resolveInstanceOf(PsiInstanceOfExpression instanceOfExpression, ResolutionContext context) {
        if (instanceOfExpression.getPattern() instanceof PsiTypeTestPattern pattern) {
            resolve(instanceOfExpression.getOperand(), context);
            var i = methodGenerator.getVariableIndex(Objects.requireNonNull(pattern.getPatternVariable()));
            methodGenerator.createDup();
            methodGenerator.createStore(i);
        } else {
            resolve(instanceOfExpression.getOperand(), context);
        }
        return methodGenerator.createInstanceOf(
                typeResolver.resolveDeclaration(requireNonNull(instanceOfExpression.getCheckType()).getType())
        );
    }

    private NodeRT resolveArrayAccess(PsiArrayAccessExpression arrayAccessExpression, ResolutionContext context) {
        resolve(arrayAccessExpression.getArrayExpression(), context);
        Values.node(methodGenerator.createNonNull());
        resolve(arrayAccessExpression.getIndexExpression(), context);
        Values.node(methodGenerator.createNonNull());
        return methodGenerator.createGetElement();
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    private boolean isBoolExpression(PsiExpression psiExpression) {
        return psiExpression.getType() == PsiType.BOOLEAN;
    }

    private NodeRT resolveThis(PsiThisExpression ignored) {
        return methodGenerator.createLoadThis();
    }

    private NodeRT resolveConditional(PsiConditionalExpression psiExpression, ResolutionContext context) {
        var i = methodGenerator.nextVariableIndex();
        constructIf(psiExpression.getCondition(), false,
                () -> {
                    resolve(psiExpression.getThenExpression(), context);
                    methodGenerator.createStore(i);
                },
                () -> {
                    resolve(psiExpression.getElseExpression(), context);
                    methodGenerator.createStore(i);
                },
                context
        );
        return methodGenerator.createLoad(i, typeResolver.resolveDeclaration(psiExpression.getType()));
    }

    private NodeRT resolveParenthesized(PsiParenthesizedExpression psiExpression, ResolutionContext context) {
        return resolve(requireNonNull(psiExpression.getExpression()), context);
    }

    private NodeRT resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        if (value == null) {
            return methodGenerator.createLoadConstant(Instances.nullInstance());
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
            case Character c -> Instances.charInstance(c);
            case null, default -> throw new InternalException("Unrecognized literal value: " + value);
        };
        return methodGenerator.createLoadConstant(instance);
    }

    private NodeRT resolveReference(PsiReferenceExpression psiReferenceExpression, ResolutionContext context) {
        var target = psiReferenceExpression.resolve();
        switch (target) {
            case PsiField psiField -> {
                if (isArrayLength(psiField)) {
                    resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    return methodGenerator.createArrayLength();
                } else {
                    PsiClass psiClass = requireNonNull(psiField.getContainingClass());
                    Field field;
                    if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                        var className = psiField.getContainingClass().getQualifiedName();
                        if (className != null && className.startsWith("java.")) {
                            var javaField = ReflectionUtils.getField(ReflectionUtils.classForName(className), psiField.getName());
                            if (PrimitiveStaticFields.isConstant(javaField))
                                return methodGenerator.createLoadConstant(Instances.fromConstant(PrimitiveStaticFields.getConstant(javaField)));
                        }
                        var klass = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass))).resolve();
                        field = Objects.requireNonNull(klass.findStaticFieldByName(psiField.getName()));
                        return methodGenerator.createGetStatic(field);
                    } else {
                        var qualifierExpr = psiReferenceExpression.getQualifierExpression();
                        resolveQualifier(qualifierExpr, context);
                        methodGenerator.createNonNull();
                        var qualifierType = qualifierExpr != null ?
                                typeResolver.resolve(qualifierExpr.getType()) : methodGenerator.getThisType();
                        Klass klass = Types.resolveKlass(qualifierType);
                        typeResolver.ensureDeclared(klass);
                        field = klass.getFieldByName(psiField.getName());
                        return methodGenerator.createGetProperty(field);
                    }
                }
            }
            case PsiMethod psiMethod -> {
                var methodRefExpr = (PsiMethodReferenceExpression) psiReferenceExpression;
                var type = typeResolver.resolveDeclaration(methodRefExpr.getFunctionalInterfaceType());
                PsiClass psiClass = requireNonNull(psiMethod.getContainingClass());
                Method method;
                if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                    Klass klass = Types.resolveKlass(typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass)));
                    method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                    methodGenerator.createGetStatic(method);
                } else {
                    if (psiReferenceExpression.getQualifierExpression() instanceof PsiReferenceExpression refExpr &&
                            refExpr.resolve() instanceof PsiClass) {
                        var klass = Types.resolveKlass(typeResolver.resolve(TranspileUtils.createType(psiClass)));
                        method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                        methodGenerator.createGetStatic(method);
                    } else {
                        var qualifierExpr = Objects.requireNonNull(psiReferenceExpression.getQualifierExpression());
                        Klass klass = Types.resolveKlass(typeResolver.resolve(qualifierExpr.getType()));
                        typeResolver.ensureDeclared(klass);
                        method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                        resolveQualifier(qualifierExpr, context);
                        methodGenerator.createGetProperty(method);
                    }
                }
                return createSAMConversion(Types.resolveKlass(type));
            }
            case PsiVariable variable -> {
                var lambda = currentLambda();
                int contextIndex;
                var type = typeResolver.resolveNullable(variable.getType(), ResolutionStage.DECLARATION);
                var index = TranspileUtils.getVariableIndex(variable);
                if (lambda != null && (contextIndex = TranspileUtils.getContextIndex(variable, lambda)) >= 0)
                    return methodGenerator.createLoadContextSlot(contextIndex, index, type);
                else
                    return methodGenerator.createLoad(index, type);
            }
            case null, default ->
                    throw new InternalException("Can not resolve reference expression with target: " + target);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private NodeRT resolveQualifier(PsiExpression qualifier, ResolutionContext context) {
        if (qualifier == null) {
            return loadThis();
        } else {
            return resolve(qualifier, context);
        }
    }

    private boolean isArrayLength(PsiField field) {
        return field.getName().equals("length") &&
                Objects.equals(requireNonNull(field.getContainingClass()).getName(), "__Array__");
    }

    private NodeRT resolvePrefix(PsiPrefixExpression psiPrefixExpression, ResolutionContext context) {
        var operand = requireNonNull(psiPrefixExpression.getOperand());
        var op = psiPrefixExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EXCL) {
            resolve(operand, context);
            return methodGenerator.createNot();
        } else if(op == JavaTokenType.MINUS) {
            resolve(operand, context);
            return methodGenerator.createNegate();
        } else if(op == JavaTokenType.PLUS) {
            return resolve(operand, context);
        } else if(op == JavaTokenType.TILDE) {
            resolve(operand, context);
            return methodGenerator.createBitNot();
        } else if (op == JavaTokenType.PLUSPLUS) {
            return resolveCompoundAssignment(operand, node -> {
                methodGenerator.createLoadConstant(Instances.longInstance(1L));
                return methodGenerator.createAdd();
            }, () -> {}, context);
        } else if (op == JavaTokenType.MINUSMINUS) {
            return resolveCompoundAssignment(operand, node -> {
                methodGenerator.createLoadConstant(Instances.longInstance(1L));
                return methodGenerator.createSub();
            }, () -> {}, context);
        } else {
            throw new InternalException("Unsupported prefix operator " + op);
        }
    }

    private NodeRT resolveUnary(PsiUnaryExpression psiUnaryExpression, ResolutionContext context) {
        if (psiUnaryExpression instanceof PsiPrefixExpression prefixExpression) {
            return resolvePrefix(prefixExpression, context);
        }
        var op = psiUnaryExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.PLUSPLUS) {
            return resolveCompoundAssignment(
                    psiUnaryExpression.getOperand(),
                    node -> node,
                    () -> {
                        methodGenerator.createLoadConstant(Instances.longInstance(1));
                        methodGenerator.createAdd();
                    },
                    context
            );
        } else if (op == JavaTokenType.MINUSMINUS) {
            return resolveCompoundAssignment(
                    psiUnaryExpression.getOperand(),
                    node -> node,
                    () -> {
                        methodGenerator.createLoadConstant(Instances.longInstance(1));
                        methodGenerator.createSub();
                    },
                    context
            );
        } else
            throw new IllegalStateException("Unrecognized unary expression: " + psiUnaryExpression.getText());
    }

    private NodeRT resolveBinary(PsiBinaryExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationSign().getTokenType();
        resolve(psiExpression.getLOperand(), context);
        resolve(psiExpression.getROperand(), context);
        return resolveBinary(op);
    }

    private NodeRT resolveBinary(IElementType op) {
        NodeRT node;
        if(op.equals(JavaTokenType.PLUS))
            node = methodGenerator.createAdd();
        else if(op.equals(JavaTokenType.MINUS))
            node = methodGenerator.createSub();
        else if(op.equals(JavaTokenType.ASTERISK))
            node = methodGenerator.createMul();
        else if(op.equals(JavaTokenType.DIV))
            node = methodGenerator.createDiv();
        else if(op.equals(JavaTokenType.LTLT))
            node = methodGenerator.createLeftShift();
        else if(op.equals(JavaTokenType.GTGT))
            node = methodGenerator.createRightShift();
        else if(op.equals(JavaTokenType.GTGTGT))
            node = methodGenerator.createUnsignedRightShift();
        else if(op.equals(JavaTokenType.OR))
            node = methodGenerator.createBitOr();
        else if(op.equals(JavaTokenType.AND))
            node = methodGenerator.createBitAnd();
        else if(op.equals(JavaTokenType.XOR))
            node = methodGenerator.createBitXor();
        else if(op.equals(JavaTokenType.ANDAND))
            node = methodGenerator.createAnd();
        else if(op.equals(JavaTokenType.OROR))
            node = methodGenerator.createOr();
        else if(op.equals(JavaTokenType.PERC))
            node = methodGenerator.createRem();
        else if(op.equals(JavaTokenType.EQEQ))
            node = methodGenerator.createEq();
        else if(op.equals(JavaTokenType.NE))
            node = methodGenerator.createNe();
        else if(op.equals(JavaTokenType.GE))
            node = methodGenerator.createGe();
        else if(op.equals(JavaTokenType.GT))
            node = methodGenerator.createGt();
        else if(op.equals(JavaTokenType.LT))
            node = methodGenerator.createLt();
        else if(op.equals(JavaTokenType.LE))
            node = methodGenerator.createLe();
        else
            throw new IllegalStateException("Unrecognized operator " + op);
        return node;
    }

    private NodeRT resolveMethodCall(PsiMethodCallExpression expression, ResolutionContext context) {
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
        var declaringType = qualifier != null && qualifier.getType() instanceof PsiClassType classType ?
                classType : null;
        var signature = TranspileUtils.getSignature(method, declaringType);
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

    private NodeRT resolveFlowCall(PsiMethodCallExpression expression, ResolutionContext context) {
        var ref = expression.getMethodExpression();
        var rawMethod = (PsiMethod) Objects.requireNonNull(ref.resolve());
        var klassName = requireNonNull(requireNonNull(rawMethod.getContainingClass()).getQualifiedName());
        if (klassName.startsWith("org.metavm.lang.")) {
            throw new InternalException("Native method should be resolved by MethodResolver: " + klassName + "." + rawMethod.getName());
        }
        var isStatic = TranspileUtils.isStatic(rawMethod);
        PsiExpression psiSelf = (PsiExpression) ref.getQualifier();
        if(!isStatic) {
            getQualifier(psiSelf, context);
            methodGenerator.createNonNull();
        }
        if (psiSelf != null) {
            if (isStatic) {
                var psiClass = (PsiClass) ((PsiReferenceExpression) psiSelf).resolve();
                ensureTypeDeclared(TranspileUtils.createType(psiClass));
            } else {
                ensureTypeDeclared(NncUtils.requireNonNull(psiSelf.getType()));
            }
        }
        var method = resolveMethod(expression);
        var psiArgs = expression.getArgumentList().getExpressions();
        for (var psiArg : psiArgs) {
             resolve(psiArg, context);
        }
        var node = methodGenerator.createMethodCall(method);
        setCapturedVariables(node);
        return node;
    }

    void setCapturedVariables(CallNode node) {
        var flow = node.getFlowRef();
        var capturedTypeSet = new HashSet<CapturedType>();
        if (flow instanceof MethodRef methodRef)
            methodRef.getDeclaringType().getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        flow.getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        var capturedTypes = new ArrayList<>(capturedTypeSet);
        var psiCapturedTypes = NncUtils.map(capturedTypes, typeResolver::getPsiCapturedType);
        var capturedPsiExpressions = NncUtils.mapAndFilterByType(psiCapturedTypes, PsiCapturedWildcardType::getContext, PsiExpression.class);
        var anchors = NncUtils.map(capturedPsiExpressions,
                e -> Objects.requireNonNull(expression2node.get(e),
                        () -> "Captured expression '" + e.getText() + "' has not yet been resolved"));
        var captureVariableTypes = NncUtils.map(
                capturedPsiExpressions, e -> typeResolver.resolveDeclaration(e.getType()));
        var capturedVariableIndexes = new ArrayList<Long>();
        for (NodeRT anchor : anchors) {
            var v = methodGenerator.nextVariableIndex();
            capturedVariableIndexes.add((long) v);
            methodGenerator.recordValue(anchor, v);
        }
        node.setCapturedVariableIndexes(capturedVariableIndexes);
        node.setCapturedVariableTypes(captureVariableTypes);
    }

    private NodeRT createSAMConversion(Klass samInterface) {
        var func = StdFunction.functionToInstance.get().getParameterized(List.of(samInterface.getType()));
        return methodGenerator.createFunctionCall(func);
    }

    @SuppressWarnings("UnusedReturnValue")
    private NodeRT getQualifier(@Nullable PsiExpression qualifierExpression, ResolutionContext context) {
        return qualifierExpression == null ? loadThis()
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
                param -> typeResolver.resolveNullable(param.getType(), ResolutionStage.DECLARATION)
        );
        var templateMethod = template.getMethodByNameAndParamTypes(method.getName(), rawParamTypes).getEffectiveVerticalTemplate();
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

    private NodeRT resolveNew(PsiNewExpression expression, ResolutionContext context) {
        if (expression.isArrayCreation()) {
            return resolveNewArray(expression, context);
        } else {
            return resolveNewPojo(expression, context);
        }
    }

    private NodeRT resolveNewArray(PsiNewExpression expression, ResolutionContext context) {
        var type = (ArrayType) typeResolver.resolveDeclaration(expression.getType());
        NodeRT node;
        if (expression.getArrayInitializer() == null) {
            NncUtils.map(expression.getArrayDimensions(), d -> resolve(d, context));
            node = methodGenerator.createNewArrayWithDimensions(type, expression.getArrayDimensions().length);
        } else {
            node = methodGenerator.createNewArray(type);
            for (var initializer : expression.getArrayInitializer().getInitializers()) {
                methodGenerator.createDup();
                resolve(initializer, context);
                methodGenerator.createAddElement();
            }
        }
        return node;
    }

    private NodeRT resolveArrayInitialization(PsiArrayInitializerExpression arrayInitializerExpression, ResolutionContext context) {
        var type = (ArrayType) typeResolver.resolveTypeOnly(arrayInitializerExpression.getType());
        var node = methodGenerator.createNewArray(type);
        for (PsiExpression initializer : arrayInitializerExpression.getInitializers()) {
            methodGenerator.createDup();
            resolve(initializer, context);
            methodGenerator.createAddElement();
        }
        return node;
    }

    private NodeRT resolveNewPojo(PsiNewExpression expression, ResolutionContext context) {
        var resolver = getNewResolver(expression);
        if (resolver != null)
            return resolver.resolve(expression, this, methodGenerator);
        if (expression.getType() instanceof PsiClassType psiClassType) {
            var klass = Types.resolveKlass(typeResolver.resolve(psiClassType));
            NncUtils.map(
                    requireNonNull(expression.getArgumentList()).getExpressions(),
                    expr -> resolve(expr, context)
            );
            var methodGenerics = expression.resolveMethodGenerics();
            NewObjectNode node;
            if (methodGenerics.getElement() == null) {
                var flow = klass.getDefaultConstructor();
                node = methodGenerator.createNew(flow, false, false);
            } else {
                var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
                var substitutor = methodGenerics.getSubstitutor();
                var paramTypes = NncUtils.map(
                        requireNonNull(method.getParameterList()).getParameters(),
                        param -> resolveParameterType(param, substitutor)
                );
                var flow = klass.getMethodByNameAndParamTypes(Types.getConstructorName(klass), paramTypes);
                node = methodGenerator.createNew(flow, false, false);
            }
            setCapturedVariables(node);
            return node;
        } else {
            // TODO support new array instance
            throw new InternalException("Unsupported NewExpression: " + expression);
        }
    }

    private Type resolveParameterType(PsiParameter param, PsiSubstitutor substitutor) {
        return typeResolver.resolveNullable(substitutor.substitute(param.getType()), ResolutionStage.INIT);
    }

    private NodeRT resolveAssignment(PsiAssignmentExpression expression, ResolutionContext context) {
        var op = expression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EQ)
            return resolveDirectAssignment(expression.getLExpression(), expression.getRExpression(), context);
        else {
            return resolveCompoundAssignment(
                    expression.getLExpression(),
                    node -> {
                        resolve(requireNonNull(expression.getRExpression()), context);
                        if (op == JavaTokenType.PLUSEQ) {
                            return methodGenerator.createAdd();
                        } else if (op == JavaTokenType.MINUSEQ) {
                            return methodGenerator.createSub();
                        } else if (op == JavaTokenType.ASTERISKEQ) {
                            return methodGenerator.createMul();
                        } else if (op == JavaTokenType.DIVEQ) {
                            return methodGenerator.createDiv();
                        } else if (op == JavaTokenType.OREQ) {
                            return methodGenerator.createBitOr();
                        } else if (op == JavaTokenType.ANDEQ) {
                            return methodGenerator.createBitAnd();
                        } else if (op == JavaTokenType.GTGTGTEQ)
                            return methodGenerator.createUnsignedRightShift();
                        else if (op == JavaTokenType.GTGTEQ)
                            return methodGenerator.createRightShift();
                        else if (op == JavaTokenType.LTLTEQ)
                            return methodGenerator.createLeftShift();
                        else {
                            throw new InternalException("Unsupported assignment operator " + op);
                        }
                    },
                    () -> {},
                    context
            );
        }
    }

    private NodeRT resolveDirectAssignment(PsiExpression assigned, PsiExpression assignment, ResolutionContext context) {
        if(assigned instanceof PsiReferenceExpression refExpr) {
            var target = refExpr.resolve();
            NodeRT node;
            if (target instanceof PsiVariable variable) {
                if (variable instanceof PsiField psiField) {
                    if (TranspileUtils.isStatic(psiField)) {
                        var klass = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.createType(psiField.getContainingClass()))).resolve();
                        var field = klass.getStaticFieldByName(psiField.getName());
                        node = resolve(assignment, context);
                        methodGenerator.createDup();
                        methodGenerator.createSetStatic(field);
                    } else {
                        //noinspection DuplicatedCode
                        ClassType instanceType;
                        if (refExpr.getQualifierExpression() != null) {
                            resolve(refExpr.getQualifierExpression(), context);
                            Values.node(methodGenerator.createNonNull());
                            instanceType = ((ClassType) typeResolver.resolveDeclaration(refExpr.getQualifierExpression().getType()));
                        } else {
                            loadThis();
                            instanceType = methodGenerator.getThisType();
                        }
                        var instanceKlass = instanceType.resolve();
                        var field = instanceKlass.getFieldByName(psiField.getName());
                        node = resolve(assignment, context);
                        methodGenerator.createDupX1();
                        methodGenerator.createSetField(field);
                    }
                } else {
                    var lambda = currentLambda();
                    int contextIndex;
                    var index = TranspileUtils.getVariableIndex(variable);
                    node = resolve(assignment, context);
                    methodGenerator.createDup();
                    if(lambda != null && (contextIndex = TranspileUtils.getContextIndex(variable, lambda)) >= 0)
                        methodGenerator.createStoreContextSlot(contextIndex, index);
                    else
                        methodGenerator.createStore(index);
                }
            } else {
                throw new InternalException("Invalid assignment target " + target);
            }
            return node;
        }
        else if(assigned instanceof PsiArrayAccessExpression arrayAccess) {
            resolve(arrayAccess.getArrayExpression(), context);
            Values.node(methodGenerator.createNonNull());
            resolve(arrayAccess.getIndexExpression(), context);
            Values.node(methodGenerator.createNonNull());
            var node = resolve(assignment, context);
            methodGenerator.createDupX2();
            methodGenerator.createSetElement();
            return node;
        }
        else
            throw new IllegalStateException("Unsupported assignment target: " + assigned);
    }

    private NodeRT resolveCompoundAssignment(PsiExpression operand,
                                             Function<NodeRT, NodeRT> action1,
                                             Runnable action2,
                                             ResolutionContext context) {
        if(operand instanceof PsiReferenceExpression refExpr) {
            var target = refExpr.resolve();
            NodeRT assignment;
            if (target instanceof PsiVariable variable) {
                if (variable instanceof PsiField psiField) {
                    if (TranspileUtils.isStatic(psiField)) {
                        var klass = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.createType(psiField.getContainingClass()))).resolve();
                        var field = klass.getStaticFieldByName(psiField.getName());
                        assignment = methodGenerator.createGetStatic(field);
                        assignment = action1.apply(assignment);
                        methodGenerator.createDup();
                        action2.run();
                        methodGenerator.createSetStatic(field);
                    } else {
                        //noinspection DuplicatedCode
                        ClassType instanceType;
                        if (refExpr.getQualifierExpression() != null) {
                            resolve(refExpr.getQualifierExpression(), context);
                            Values.node(methodGenerator.createNonNull());
                            instanceType = ((ClassType) typeResolver.resolveDeclaration(refExpr.getQualifierExpression().getType()));
                        } else {
                            loadThis();
                            instanceType = methodGenerator.getThisType();
                        }
                        methodGenerator.createDup();
                        var instanceKlass = instanceType.resolve();
                        var field = instanceKlass.getFieldByName(psiField.getName());
                        assignment = methodGenerator.createGetProperty(field);
                        assignment = action1.apply(assignment);
                        methodGenerator.createDupX1();
                        action2.run();
                        methodGenerator.createSetField(field);
                    }
                } else {
                    var lambda = currentLambda();
                    int contextIndex = -1;
                    var captured = lambda != null && (contextIndex = TranspileUtils.getContextIndex(variable, lambda)) >= 0;
                    var index = TranspileUtils.getVariableIndex(variable);
                    var type = typeResolver.resolveDeclaration(operand.getType());
                    if(captured)
                        assignment = methodGenerator.createLoadContextSlot(contextIndex, index, type);
                    else
                        assignment = methodGenerator.createLoad(index, type);
                    assignment = action1.apply(assignment);
                    methodGenerator.createDup();
                    action2.run();
                    if(captured)
                        methodGenerator.createStoreContextSlot(contextIndex, index);
                    else
                        methodGenerator.createStore(index);
                }
            } else {
                throw new InternalException("Invalid assignment target " + target);
            }
            return assignment;
        }
        else if(operand instanceof PsiArrayAccessExpression arrayAccess) {
            resolve(arrayAccess.getArrayExpression(), context);
            Values.node(methodGenerator.createNonNull());
            methodGenerator.createDup();
            resolve(arrayAccess.getIndexExpression(), context);
            Values.node(methodGenerator.createNonNull());
            methodGenerator.createDupX1();
            NodeRT assignment = methodGenerator.createGetElement();
            assignment = action1.apply(assignment);
            methodGenerator.createDupX2();
            action2.run();
            methodGenerator.createSetElement();
            return assignment;
        }
        else
            throw new IllegalStateException("Unsupported assignment target: " + operand);
    }

    private BinaryOperator resolveOperator(PsiJavaToken psiOp) {
        return resolveOperator(psiOp.getTokenType());
    }

    private BinaryOperator resolveOperator(IElementType elementType) {
        return OPERATOR_MAP.get(elementType);
    }

    @SuppressWarnings("UnusedReturnValue")
    public NodeRT constructIf(PsiExpression condition, Runnable thenAction, Runnable elseAction) {
        return constructIf(condition, false, thenAction, elseAction, new ResolutionContext());
    }

    public NodeRT constructIf(PsiExpression condition, boolean negated, Runnable thenAction, Runnable elseAction, ResolutionContext context) {
        return switch (condition) {
            case PsiBinaryExpression binaryExpression ->
                    constructBinaryIf(binaryExpression, negated, thenAction, elseAction, context);
            case PsiUnaryExpression unaryExpression -> constructUnaryIf(unaryExpression, negated, thenAction, elseAction, context);
            case PsiPolyadicExpression polyadicExpression ->
                    constructPolyadicIf(polyadicExpression, negated, thenAction, elseAction, context);
            default -> constructAtomicIf(condition, negated, thenAction, elseAction, context);
        };
    }

    private NodeRT constructUnaryIf(PsiUnaryExpression unaryExpression,
                                          boolean negated,
                                          Runnable thenAction,
                                          Runnable elseAction,
                                          ResolutionContext context) {
        var op = unaryExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EXCL) {
            return constructIf(requireNonNull(unaryExpression.getOperand()), !negated, thenAction, elseAction, context);
        } else {
            throw new InternalException("Invalid unary operator for bool expression: " + op);
        }
    }

    private NodeRT constructBinaryIf(PsiBinaryExpression condition, boolean negated, Runnable thenAction, Runnable elseAction, ResolutionContext context) {
        var op = resolveOperator(condition.getOperationSign());
        if(op == BinaryOperator.AND || op == BinaryOperator.OR)
            return constructPolyadicIf(condition, negated, thenAction, elseAction, context);
        else
            return constructAtomicIf(condition, negated, thenAction, elseAction, context);
    }

    private NodeRT constructPolyadicIf(PsiPolyadicExpression condition, boolean negated, Runnable thenAction, Runnable elseAction, ResolutionContext context) {
        var op = resolveOperator(condition.getOperationTokenType());
        if (op == BinaryOperator.AND) {
            return constructAndPolyadicIf(condition.getOperands(),  0, negated, thenAction, elseAction, context);
        } else if (op == BinaryOperator.OR) {
            return constructOrPolyadicIf(condition.getOperands(),  0, negated, thenAction, elseAction, context);
        } else {
            throw new InternalException("Invalid operator for polyadic boolean expression: " + op);
        }
    }

    private NodeRT constructAndPolyadicIf(PsiExpression[] items,
                                                int index,
                                                boolean negated,
                                                Runnable thenAction,
                                                Runnable elseAction,
                                                ResolutionContext context
    ) {
        if(index == items.length - 1)
            return constructIf(items[index], negated, thenAction, elseAction, context);
        else {
            var ref = new Object() {GotoNode gotoNode;};
            return constructIf(items[index],
                    negated,
                    () -> constructAndPolyadicIf(items,
                            index + 1,
                            negated,
                            thenAction,
                            () -> ref.gotoNode = methodGenerator.createIncompleteGoto(),
                            context),
                    () -> {
                        var mergeNode = methodGenerator.createTarget();
                        ref.gotoNode.setTarget(mergeNode);
                        elseAction.run();
                    }, context);
        }
    }

    private NodeRT constructOrPolyadicIf(PsiExpression[] items,
                                               int index,
                                               boolean negated,
                                               Runnable thenAction, Runnable elseAction,
                                               ResolutionContext context
    ) {
        if(index == items.length - 1)
            return constructIf(items[index], negated, thenAction, elseAction, context);
        else {
            var ref = new Object() {
                NodeRT target;
            };
            return constructIf(items[index],
                    negated,
                    () -> {
                        ref.target = methodGenerator.createTarget();
                        thenAction.run();
                    },
                    () -> constructOrPolyadicIf(
                            items, index+1,
                            negated,
                            () -> methodGenerator.createGoto(ref.target),
                            elseAction,
                            context
                    ),
                    context);
        }
    }

    private NodeRT constructAtomicIf(PsiExpression condition, boolean negated, Runnable thenAction,
                                           Runnable elseAction, ResolutionContext context) {
        resolveNormal(condition, context);
        var ifNode = negated ? methodGenerator.createIf(null)
                : methodGenerator.createIfNot(null);
        thenAction.run();
        var g = methodGenerator.createGoto(null);
        ifNode.setTarget(methodGenerator.createNoop());
        elseAction.run();
        var join = methodGenerator.createNoop();
        g.setTarget(join);
        return join;
    }

    public NodeRT resolveBoolExpr(PsiExpression expression, ResolutionContext context) {
        var i = methodGenerator.nextVariableIndex();
         constructIf(expression, false,
                () -> {
                    methodGenerator.createLoadConstant(Instances.trueInstance());
                    methodGenerator.createStore(i);
                },
                () -> {
                    methodGenerator.createLoadConstant(Instances.falseInstance());
                    methodGenerator.createStore(i);
                }, context);
        return methodGenerator.createLoad(i, Types.getBooleanType());
    }

    public NodeRT resolveLambdaExpression(PsiLambdaExpression expression, ResolutionContext context) {
        enterLambda(expression);
        var returnType = typeResolver.resolveNullable(TranspileUtils.getLambdaReturnType(expression), ResolutionStage.DECLARATION);
        var parameters = new ArrayList<Parameter>();
        int i = 0;
        for (var psiParameter : expression.getParameterList().getParameters()) {
            parameters.add(resolveParameter(psiParameter));
            psiParameter.putUserData(Keys.VARIABLE_INDEX, i++);
        }
        var funcInterface = Types.resolveKlass(typeResolver.resolveDeclaration(expression.getFunctionalInterfaceType()));
        var lambda = new Lambda(null, parameters, returnType, methodGenerator.getMethod());
        methodGenerator.enterScope(lambda.getScope());
        if (expression.getBody() instanceof PsiExpression bodyExpr) {
            resolve(bodyExpr, context);
            if(returnType.isVoid())
                methodGenerator.createVoidReturn();
            else
                methodGenerator.createReturn();
        } else {
            requireNonNull(expression.getBody()).accept(visitor);
            if (lambda.getReturnType().isVoid()) {
                var lastNode = methodGenerator.scope().getLastNode();
                if (lastNode == null || !lastNode.isExit())
                    methodGenerator.createVoidReturn();
            }
        }
        methodGenerator.exitScope();
        exitLambda();
        return methodGenerator.createLambda(lambda, funcInterface);
    }

    private NodeRT resolveSwitchExpression(PsiSwitchExpression psiSwitchExpression, ResolutionContext context) {
        var y = methodGenerator.enterSwitchExpression();
        var switchVarIndex = methodGenerator.nextVariableIndex();
        resolve(psiSwitchExpression.getExpression(), context);
        methodGenerator.createStore(switchVarIndex);
        var switchVarType = typeResolver.resolveDeclaration(psiSwitchExpression.getType());
        var body = requireNonNull(psiSwitchExpression.getBody());
        var statements = requireNonNull(body.getStatements());
        List<IfNotNode> lastIfNodes = List.of();
        GotoNode lastGoto = null;
        var gotoNodes = new ArrayList<GotoNode>();
        var type = typeResolver.resolveDeclaration(psiSwitchExpression.getType());
        for (PsiStatement statement : statements) {
            if (statement instanceof PsiSwitchLabeledRuleStatement labeledRuleStatement) {
                if(labeledRuleStatement.isDefaultCase())
                    continue;
                var caseElementList = requireNonNull(labeledRuleStatement.getCaseLabelElementList());
                var ifNodes = new ArrayList<IfNotNode>();
                for (PsiCaseLabelElement element : caseElementList.getElements()) {
                    ifNodes.add(resolveSwitchCaseLabel(switchVarIndex, switchVarType, element, context));
                }
                if(lastGoto != null) {
                    for (IfNotNode lastIfNode : lastIfNodes) {
                        lastIfNode.setTarget(lastGoto.getSuccessor());
                    }
                }
                lastIfNodes = ifNodes;
                var caseBody = requireNonNull(labeledRuleStatement.getBody());
                processSwitchCaseBody(caseBody, context);
                var lastNode = Objects.requireNonNull(methodGenerator.scope().getLastNode());
                if(lastNode.isExit())
                    lastGoto = null;
                else
                    gotoNodes.add(lastGoto = methodGenerator.createGoto(null));
            }
        }
        var defaultStmt = (PsiSwitchLabeledRuleStatement) NncUtils.findRequired(statements,
                stmt -> stmt instanceof PsiSwitchLabeledRuleStatement ruleStmt && ruleStmt.isDefaultCase());
        var defaultCase = methodGenerator.createNoop();
        lastIfNodes.forEach(n -> n.setTarget(defaultCase));
        processSwitchCaseBody(defaultStmt.getBody(), context);
        var joinNode = methodGenerator.createNoop();
        gotoNodes.forEach(g -> g.setTarget(joinNode));
        methodGenerator.exitSwitchExpression();
        return methodGenerator.createLoad(y, type);
    }

    private IfNotNode resolveSwitchCaseLabel(int switchVarIndex, Type switchVarType, PsiElement label, ResolutionContext context) {
        if(label instanceof PsiExpression expression) {
            methodGenerator.createLoad(switchVarIndex, switchVarType);
            resolve(expression, context);
            methodGenerator.createEq();
            return methodGenerator.createIfNot(null);
        }
        if(label instanceof PsiTypeTestPattern typeTestPattern) {
            var checkType = requireNonNull(typeTestPattern.getCheckType()).getType();
           methodGenerator.createLoad(switchVarIndex, switchVarType);
            methodGenerator.createStore(
                    methodGenerator.getVariableIndex(Objects.requireNonNull(typeTestPattern.getPatternVariable()))
            );
            methodGenerator.createLoad(switchVarIndex, switchVarType);
            methodGenerator.createInstanceOf(typeResolver.resolveDeclaration(checkType));
            return methodGenerator.createIfNot(null);
        }
        throw new IllegalArgumentException("Invalid switch case: " + label);
    }

    private void processSwitchCaseBody(PsiElement caseBody, ResolutionContext context) {
        if (caseBody instanceof PsiExpressionStatement exprStmt) {
            resolve(exprStmt.getExpression(), context);
            methodGenerator.createYieldStore();
        }
        else
            caseBody.accept(visitor);
    }

    private Parameter resolveParameter(PsiParameter psiParameter) {
        return new Parameter(
                null, psiParameter.getName(),
                typeResolver.resolveNullable(psiParameter.getType(), ResolutionStage.DECLARATION)
        );
    }

    void enterLambda(PsiLambdaExpression lambda) {
        lambdas.push(lambda);
    }

    void exitLambda() {
        lambdas.pop();
    }

    @Nullable PsiLambdaExpression currentLambda() {
        return lambdas.peek();
    }

    NodeRT loadThis() {
        assert !methodGenerator.getMethod().isStatic();
        var lambda = currentLambda();
        if(lambda == null)
            return methodGenerator.createLoadThis();
        else {
            return methodGenerator.createLoadContextSlot(
                    TranspileUtils.getMethodContextIndex(lambda), 0, methodGenerator.getThisType());
        }
    }

    public static class ResolutionContext {
    }

}
