package org.metavm.autograph;

import com.google.common.collect.Streams;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.BinaryOperator;
import org.metavm.expression.Expression;
import org.metavm.expression.NodeExpression;
import org.metavm.expression.UnaryOperator;
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

import static java.util.Objects.requireNonNull;

public class ExpressionResolver {

    public static final Logger logger = LoggerFactory.getLogger(ExpressionResolver.class);

    public static final Map<IElementType, UnaryOperator> UNARY_OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.PLUS, UnaryOperator.POS),
            Map.entry(JavaTokenType.MINUS, UnaryOperator.NEG),
            Map.entry(JavaTokenType.EXCL, UnaryOperator.NOT),
            Map.entry(JavaTokenType.TILDE, UnaryOperator.BITWISE_COMPLEMENT)
    );

    private static final Map<IElementType, BinaryOperator> OPERATOR_MAP = Map.ofEntries(
            Map.entry(JavaTokenType.ASTERISK, BinaryOperator.MULTIPLY),
            Map.entry(JavaTokenType.DIV, BinaryOperator.DIVIDE),
            Map.entry(JavaTokenType.PLUS, BinaryOperator.ADD),
            Map.entry(JavaTokenType.MINUS, BinaryOperator.MINUS),
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

    public static final Set<IElementType> BOOL_OPS = Set.of(
            JavaTokenType.ANDAND, JavaTokenType.OROR
    );

    private final MethodGenerator methodGenerator;
    private final TypeResolver typeResolver;
    private final VisitorBase visitor;
    private final Map<PsiExpression, Expression> expressionMap = new IdentityHashMap<>();
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

    public org.metavm.flow.Value resolve(PsiExpression psiExpression) {
        ResolutionContext context = new ResolutionContext();
        try {
            return resolve(psiExpression, context);
        }
        catch (Exception e) {
            throw new InternalException("Failed to resolve expression: " + psiExpression.getClass().getSimpleName() + " " + psiExpression.getText(), e);
        }
    }

    private org.metavm.flow.Value resolve(PsiExpression psiExpression, ResolutionContext context) {
        org.metavm.flow.Value resolved;
        if (isBoolExpression(psiExpression)) {
            resolved = resolveBoolExpr(psiExpression, context);
        } else {
            resolved = resolveNormal(psiExpression, context);
        }
        if(resolved != null && !expressionMap.containsKey(psiExpression))
            expressionMap.put(psiExpression, resolved.getExpression());
//        if(resolved != null && resolved.getType().isCaptured())
//            TranspileUtil.forEachCapturedTypePairs(psiExpression.getType(), resolved.getType(), typeResolver::mapCapturedType);
        return resolved;
    }

    private CapturedType resolveCapturedType(PsiCapturedWildcardType psiCapturedWildcardType) {
        return (CapturedType) expressionMap.get((PsiExpression) psiCapturedWildcardType.getContext()).getType();
    }

    private org.metavm.flow.Value resolveNormal(PsiExpression psiExpression, ResolutionContext context) {
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

    private org.metavm.flow.Value resolveSuper(PsiSuperExpression superExpression) {
        return getThis();
    }

    private org.metavm.flow.Value resolveClassObjectAccess(PsiClassObjectAccessExpression classObjectAccessExpression, ResolutionContext context) {
        var type = typeResolver.resolveTypeOnly(classObjectAccessExpression.getOperand().getType());
        return Values.type(type);
    }

    private org.metavm.flow.Value resolveTypeCast(PsiTypeCastExpression typeCastExpression, ResolutionContext context) {
        var operand = resolve(typeCastExpression.getOperand(), context);
        var targetType = typeResolver.resolveDeclaration(requireNonNull(typeCastExpression.getCastType()).getType());
        return Values.node(methodGenerator.createTypeCast(operand, targetType));
    }

    private org.metavm.flow.Value resolvePolyadic(PsiPolyadicExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationTokenType();
        var operands = psiExpression.getOperands();
        var current = resolve(operands[0], context);
        for (int i = 1; i < operands.length; i++) {
            current = resolveBinary(op, current, resolve(operands[i], context));
        }
        return current;
    }

    private org.metavm.flow.Value resolveInstanceOf(PsiInstanceOfExpression instanceOfExpression, ResolutionContext context) {
        org.metavm.flow.Value operand;
        if (instanceOfExpression.getPattern() instanceof PsiTypeTestPattern pattern) {
            operand = resolve(instanceOfExpression.getOperand(), context);
            var i = methodGenerator.getVariableIndex(Objects.requireNonNull(pattern.getPatternVariable()));
            methodGenerator.createStore(i, operand);
        } else {
            operand = resolve(instanceOfExpression.getOperand(), context);
        }
        return Values.node(methodGenerator.createInstanceOf(
                operand,
                typeResolver.resolveDeclaration(requireNonNull(instanceOfExpression.getCheckType()).getType())
        ));
    }

    private org.metavm.flow.Value resolveArrayAccess(PsiArrayAccessExpression arrayAccessExpression, ResolutionContext context) {
        var array = resolve(arrayAccessExpression.getArrayExpression(), context);
        var index = resolve(arrayAccessExpression.getIndexExpression(), context);
        if (methodGenerator.getExpressionType(array.getExpression()).isNullable())
            array = Values.node(methodGenerator.createNonNull("nonNull", array));
        if (methodGenerator.getExpressionType(index.getExpression()).isNullable())
            index = Values.node(methodGenerator.createNonNull("nonNull", index));
        return Values.node(methodGenerator.createGetElement(array, index));
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    private boolean isBoolExpression(PsiExpression psiExpression) {
        return psiExpression.getType() == PsiType.BOOLEAN;
    }

    private org.metavm.flow.Value resolveThis(PsiThisExpression ignored) {
        return getThis();
    }

    private org.metavm.flow.Value resolveConditional(PsiConditionalExpression psiExpression, ResolutionContext context) {
        var i = methodGenerator.nextVariableIndex();
        constructIf(psiExpression.getCondition(), false,
                () -> methodGenerator.createStore(i, resolve(psiExpression.getThenExpression(), context)),
                () -> methodGenerator.createStore(i, resolve(psiExpression.getElseExpression(), context)),
                context
        );
        return Values.node(methodGenerator.createLoad(i, typeResolver.resolveDeclaration(psiExpression.getType())));
    }

    private org.metavm.flow.Value resolveParenthesized(PsiParenthesizedExpression psiExpression, ResolutionContext context) {
        return resolve(requireNonNull(psiExpression.getExpression()), context);
    }

    private org.metavm.flow.Value resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        if (value == null) {
            return Values.nullValue();
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
        return Values.constant(instance);
    }

    private org.metavm.flow.Value resolveReference(PsiReferenceExpression psiReferenceExpression, ResolutionContext context) {
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
                return Values.node(methodGenerator.createArrayLength(arrayExpr));
            } else {
                PsiClass psiClass = requireNonNull(psiField.getContainingClass());
                Field field;
                if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                    var className = psiField.getContainingClass().getQualifiedName();
                    if(className != null && className.startsWith("java.")) {
                        var javaField = ReflectionUtils.getField(ReflectionUtils.classForName(className), psiField.getName());
                        if(PrimitiveStaticFields.isConstant(javaField))
                            return Values.constant(Instances.fromConstant(PrimitiveStaticFields.getConstant(javaField)));
                    }
                    var klass = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass))).resolve();
                    field = Objects.requireNonNull(klass.findStaticFieldByCode(psiField.getName()));
                    return Values.node(methodGenerator.createGetStatic(field));
                } else {
                    var qualifierExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    if(methodGenerator.getExpressionType(qualifierExpr.getExpression()).isNullable())
                        qualifierExpr = Values.node(methodGenerator.createNonNull("nonNull", qualifierExpr));
                    Klass klass = Types.resolveKlass(methodGenerator.getExpressionType(qualifierExpr.getExpression()));
                    typeResolver.ensureDeclared(klass);
                    field = klass.getFieldByCode(psiField.getName());
                    return Values.node(methodGenerator.createGetProperty(qualifierExpr, field));
                }
            }
        } else if (target instanceof PsiMethod psiMethod) {
            PsiClass psiClass = requireNonNull(psiMethod.getContainingClass());
            Method method;
            if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                Klass klass = Types.resolveKlass(typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass)));
                method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                return Values.node(methodGenerator.createGetStatic(method));
            } else {
                if (psiReferenceExpression.getQualifierExpression() instanceof PsiReferenceExpression refExpr &&
                        refExpr.resolve() instanceof PsiClass) {
                    var klass = Types.resolveKlass(typeResolver.resolve(TranspileUtils.createType(psiClass)));
                    method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                    return Values.node(methodGenerator.createGetStatic(method));
                } else {
                    var qualifierExpr = resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    Klass klass = Types.resolveKlass(methodGenerator.getExpressionType(qualifierExpr.getExpression()));
                    typeResolver.ensureDeclared(klass);
                    method = klass.getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                    return Values.node(methodGenerator.createGetProperty(qualifierExpr, method));
                }
            }
        } else if (target instanceof PsiVariable variable) {
            var lambda = currentLambda();
            int contextIndex;
            var type = typeResolver.resolveNullable(variable.getType(), ResolutionStage.DECLARATION);
            var index = TranspileUtils.getVariableIndex(variable);
            if(lambda != null && (contextIndex = TranspileUtils.getContextIndex(variable, lambda)) >= 0)
                return Values.node(methodGenerator.createLoadContextSlot(contextIndex, index, type));
            else
                return Values.node(methodGenerator.createLoad(index, type));
        } else {
            throw new InternalException("Can not resolve reference expression with target: " + target);
        }
    }

    private boolean isClassRef(PsiExpression psiExpression) {
        return psiExpression instanceof PsiReferenceExpression refExpr &&
                refExpr.resolve() instanceof PsiClass;
    }

    private org.metavm.flow.Value resolveQualifier(PsiExpression qualifier, ResolutionContext context) {
        if (qualifier == null) {
            return getThis();
        } else {
            return resolve(qualifier, context);
        }
    }

    private boolean isArrayLength(PsiField field) {
        return field.getName().equals("length") &&
                Objects.equals(requireNonNull(field.getContainingClass()).getName(), "__Array__");
    }

    private org.metavm.flow.Value resolvePrefix(PsiPrefixExpression psiPrefixExpression, ResolutionContext context) {
        var operand = NncUtils.requireNonNull(psiPrefixExpression.getOperand());
        var resolvedOperand = resolve(requireNonNull(psiPrefixExpression.getOperand()), context);
        var op = psiPrefixExpression.getOperationSign().getTokenType();
        if (op == JavaTokenType.EXCL) {
            return Values.node(methodGenerator.createNot(resolvedOperand));
        }
        if(op == JavaTokenType.MINUS)
            return Values.node(methodGenerator.createNegate(resolvedOperand));
        if(op == JavaTokenType.PLUS)
            return resolvedOperand;
        if(op == JavaTokenType.TILDE)
            return Values.node(methodGenerator.createBitwiseComplement(resolvedOperand));
        if (op == JavaTokenType.PLUSPLUS) {
            var assignment = Values.node(methodGenerator.createAdd(
                    resolvedOperand, Values.constantLong(1L)
            ));
            processAssignment(operand, assignment, context);
            return assignment;
        } else if (op == JavaTokenType.MINUSMINUS) {
            var assignment = Values.node(methodGenerator.createSub(
                    resolvedOperand, Values.constantLong(1L)
            ));
            processAssignment(operand, assignment, context);
            return assignment;
        } else {
            throw new InternalException("Unsupported prefix operator " + op);
        }
    }

    private org.metavm.flow.Value resolveUnary(PsiUnaryExpression psiUnaryExpression, ResolutionContext context) {
//        if (psiUnaryExpression.getOperand() instanceof PsiLiteralExpression literalExpression) {
//            return new UnaryExpression(
//                    resolveUnaryOperator(psiUnaryExpression.getOperationSign().getTokenType()),
//                    resolveLiteral(literalExpression)
//            );
//        }
        if (psiUnaryExpression instanceof PsiPrefixExpression prefixExpression) {
            return resolvePrefix(prefixExpression, context);
        }
        var op = psiUnaryExpression.getOperationSign().getTokenType();
        var resolvedOperand = resolve(requireNonNull(psiUnaryExpression.getOperand()), context);
        if (op == JavaTokenType.PLUSPLUS) {
            processAssignment(
                    psiUnaryExpression.getOperand(),
                    Values.node(methodGenerator.createAdd(resolvedOperand, Values.constantLong(1L))),
                    context
            );
            return resolvedOperand;
        } else if (op == JavaTokenType.MINUSMINUS) {
            processAssignment(
                    psiUnaryExpression.getOperand(),
                    Values.node(methodGenerator.createSub(resolvedOperand, Values.constantLong(1L))),
                    context
            );
            return resolvedOperand;
        } else
            throw new IllegalStateException("Unrecognized unary expression: " + psiUnaryExpression.getText());
    }

    private org.metavm.flow.Value resolveBinary(PsiBinaryExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationSign().getTokenType();
        var first = resolve(psiExpression.getLOperand(), context);
        var second = resolve(psiExpression.getROperand(), context);
        return resolveBinary(op, first, second);
    }

    private org.metavm.flow.Value resolveBinary(IElementType op, org.metavm.flow.Value first, org.metavm.flow.Value second) {
        NodeRT node;
        if(op.equals(JavaTokenType.PLUS))
            node = methodGenerator.createAdd(first, second);
        else if(op.equals(JavaTokenType.MINUS))
            node = methodGenerator.createSub(first, second);
        else if(op.equals(JavaTokenType.ASTERISK))
            node = methodGenerator.createMul(first, second);
        else if(op.equals(JavaTokenType.DIV))
            node = methodGenerator.createDiv(first, second);
        else if(op.equals(JavaTokenType.LTLT))
            node = methodGenerator.createLeftShift(first, second);
        else if(op.equals(JavaTokenType.GTGT))
            node = methodGenerator.createRightShift(first, second);
        else if(op.equals(JavaTokenType.GTGTGT))
            node = methodGenerator.createUnsignedRightShift(first, second);
        else if(op.equals(JavaTokenType.OR))
            node = methodGenerator.createBitwiseOr(first, second);
        else if(op.equals(JavaTokenType.AND))
            node = methodGenerator.createBitwiseAnd(first, second);
        else if(op.equals(JavaTokenType.XOR))
            node = methodGenerator.createBitwiseXor(first, second);
        else if(op.equals(JavaTokenType.ANDAND))
            node = methodGenerator.createAnd(first, second);
        else if(op.equals(JavaTokenType.OROR))
            node = methodGenerator.createOr(first, second);
        else if(op.equals(JavaTokenType.PERC))
            node = methodGenerator.createRem(first, second);
        else if(op.equals(JavaTokenType.EQEQ))
            node = methodGenerator.createEq(first, second);
        else if(op.equals(JavaTokenType.NE))
            node = methodGenerator.createNe(first, second);
        else if(op.equals(JavaTokenType.GE))
            node = methodGenerator.createGe(first, second);
        else if(op.equals(JavaTokenType.GT))
            node = methodGenerator.createGt(first, second);
        else if(op.equals(JavaTokenType.LT))
            node = methodGenerator.createLt(first, second);
        else if(op.equals(JavaTokenType.LE))
            node = methodGenerator.createLe(first, second);
        else
            throw new IllegalStateException("Unrecognized operator " + op);
        return Values.node(node);
    }

    private org.metavm.flow.Value resolveMethodCall(PsiMethodCallExpression expression, ResolutionContext context) {
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

    private MethodCallNode createInvokeFlowNode(org.metavm.flow.Value self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
        List<org.metavm.flow.Value> arguments = NncUtils.map(argumentList.getExpressions(), arg -> resolve(arg, context));
        var exprType = Types.resolveKlass(methodGenerator.getExpressionType(self.getExpression()));
        typeResolver.ensureDeclared(exprType);
        return methodGenerator.createMethodCall(self, Objects.requireNonNull(exprType.findMethodByCode(flowCode)), arguments);
    }

    private Expression invokeFlow(org.metavm.flow.Value self, String flowCode, PsiExpressionList argumentList, ResolutionContext context) {
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

    private org.metavm.flow.Value resolveFlowCall(PsiMethodCallExpression expression, ResolutionContext context) {
        var ref = expression.getMethodExpression();
        var rawMethod = (PsiMethod) Objects.requireNonNull(ref.resolve());
        var klassName = requireNonNull(requireNonNull(rawMethod.getContainingClass()).getQualifiedName());
        if (klassName.startsWith("org.metavm.lang.")) {
            throw new InternalException("Native method should be resolved by MethodResolver: " + klassName + "." + rawMethod.getName());
        }
        var isStatic = TranspileUtils.isStatic(rawMethod);
        PsiExpression psiSelf = (PsiExpression) ref.getQualifier();
        org.metavm.flow.Value qualifier;
        if(isStatic)
            qualifier = null;
        else {
            qualifier = getQualifier(psiSelf, context);
            if(methodGenerator.getExpressionType(qualifier.getExpression()).isNullable())
                qualifier = Values.node(methodGenerator.createNonNull("nonNull", qualifier));
        }
        if (psiSelf != null) {
            if (isStatic) {
                var psiClass = (PsiClass) ((PsiReferenceExpression) psiSelf).resolve();
                ensureTypeDeclared(TranspileUtils.createType(psiClass));
            } else {
                ensureTypeDeclared(NncUtils.requireNonNull(psiSelf.getType()));
            }
        }
        var substitutor = expression.resolveMethodGenerics().getSubstitutor();
        var method = resolveMethod(expression);
        var psiArgs = expression.getArgumentList().getExpressions();
        var args = new ArrayList<org.metavm.flow.Value>();
        for (var psiArg : psiArgs) {
            var arg = resolve(psiArg, context);
            var paramType = typeResolver.resolveDeclaration(substitutor.substitute(psiArg.getType()));
            if (paramType instanceof ClassType classType) {
                var klass = classType.resolve();
                if (klass.isSAMInterface()
                        && arg.getType() instanceof FunctionType) {
                    arg = Values.node(createSAMConversion(klass, arg));
                }
            }
            args.add(arg);
        }
        var node = methodGenerator.createMethodCall(qualifier, method, args);
        setCapturedExpressions(node, context);
        if (method.getReturnType().isVoid()) {
            return null;
        } else {
            return Values.node(node);
        }
    }

    void setCapturedExpressions(CallNode node, ResolutionContext context) {
        var flow = node.getFlowRef();
        var capturedTypeSet = new HashSet<CapturedType>();
        if (flow instanceof MethodRef methodRef)
            methodRef.getDeclaringType().getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        flow.getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        var capturedTypes = new ArrayList<>(capturedTypeSet);
        var psiCapturedTypes = NncUtils.map(capturedTypes, typeResolver::getPsiCapturedType);
        var capturedPsiExpressions = NncUtils.mapAndFilterByType(psiCapturedTypes, PsiCapturedWildcardType::getContext, PsiExpression.class);
        var capturedExpressions = NncUtils.map(capturedPsiExpressions,
                e -> Objects.requireNonNull(expressionMap.get(e),
                        () -> "Captured expression '" + e.getText() + "' has not yet been resolved"));
        var captureExpressionTypes = NncUtils.map(
                capturedPsiExpressions, e -> typeResolver.resolveDeclaration(e.getType()));
        node.setCapturedExpressions(capturedExpressions);
        node.setCapturedExpressionTypes(captureExpressionTypes);
    }

    private NodeRT createSAMConversion(Klass samInterface, org.metavm.flow.Value function) {
        var func = StdFunction.functionToInstance.get().getParameterized(List.of(samInterface.getType()));
        return methodGenerator.createFunctionCall(func, List.of(function));
    }

    private org.metavm.flow.Value getQualifier(@Nullable PsiExpression qualifierExpression, ResolutionContext context) {
        return qualifierExpression == null ? getThis()
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

    private org.metavm.flow.Value resolveNew(PsiNewExpression expression, ResolutionContext context) {
        if (expression.isArrayCreation()) {
            return resolveNewArray(expression, context);
        } else {
            return resolveNewPojo(expression, context);
        }
    }

    private org.metavm.flow.Value resolveNewArray(PsiNewExpression expression, ResolutionContext context) {
        var type = (ArrayType) typeResolver.resolveDeclaration(expression.getType());
        NewArrayNode node;
        if (expression.getArrayInitializer() == null) {
            node = methodGenerator.createNewArrayWithDimensions(
                    type,
                    NncUtils.map(expression.getArrayDimensions(), d -> resolve(d, context))
            );
        } else {
            node = methodGenerator.createNewArray(
                    type,
                    Values.array(
                            NncUtils.map(
                                    expression.getArrayInitializer().getInitializers(),
                                    e -> resolve(e, context)
                            ),
                            type
                    )
            );
        }
        return Values.node(node);
    }

    private org.metavm.flow.Value resolveArrayInitialization(PsiArrayInitializerExpression arrayInitializerExpression, ResolutionContext context) {
        var type = (ArrayType) typeResolver.resolveTypeOnly(arrayInitializerExpression.getType());
        return Values.node(methodGenerator.createNewArray(
                type,
                Values.array(
                        NncUtils.map(
                                arrayInitializerExpression.getInitializers(),
                                e -> resolve(e, context)
                        ),
                        type
                )
        ));
    }

    private org.metavm.flow.Value resolveNewPojo(PsiNewExpression expression, ResolutionContext context) {
        var resolver = getNewResolver(expression);
        if (resolver != null)
            return resolver.resolve(expression, this, methodGenerator);
        if (expression.getType() instanceof PsiClassType psiClassType) {
            var klass = Types.resolveKlass(typeResolver.resolve(psiClassType));
            List<org.metavm.flow.Value> args = NncUtils.map(
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

    private List<org.metavm.flow.Value> resolveExpressionList(PsiExpressionList expressionList, ResolutionContext context) {
        return NncUtils.map(expressionList.getExpressions(), expression -> resolve(expression, context));
    }

    public org.metavm.flow.Value newInstance(Klass declaringType, List<org.metavm.flow.Value> arguments,
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
        return Values.node(node);
    }

    private Type resolveParameterType(PsiParameter param, PsiSubstitutor substitutor) {
        return typeResolver.resolveNullable(substitutor.substitute(param.getType()), ResolutionStage.INIT);
    }

    private Expression createNodeExpression(NodeRT node) {
        return new NodeExpression(node);
    }

    private org.metavm.flow.Value resolveAssignment(PsiAssignmentExpression expression, ResolutionContext context) {
        var op = expression.getOperationSign().getTokenType();
        var resolvedRight = resolve(requireNonNull(expression.getRExpression()), context);
        org.metavm.flow.Value assignment;
        if (op == JavaTokenType.EQ) {
            assignment = resolvedRight;
        } else {
            NodeRT node;
            var resolvedLeft = resolve(expression.getLExpression(), context);
            if (op == JavaTokenType.PLUSEQ) {
                node = methodGenerator.createAdd(resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.MINUSEQ) {
                node = methodGenerator.createSub(resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.ASTERISKEQ) {
                node = methodGenerator.createMul(resolvedLeft, resolvedRight);
            } else if (op == JavaTokenType.DIVEQ) {
                node = methodGenerator.createDiv(resolvedLeft, resolvedRight);
            } else if(op == JavaTokenType.OREQ) {
                node = methodGenerator.createBitwiseOr(resolvedLeft, resolvedRight);
            } else if(op == JavaTokenType.ANDEQ) {
                node = methodGenerator.createBitwiseAnd(resolvedLeft, resolvedRight);
            } else if(op == JavaTokenType.GTGTGTEQ)
                node = methodGenerator.createUnsignedRightShift(resolvedLeft, resolvedRight);
            else if(op == JavaTokenType.GTGTEQ)
                node = methodGenerator.createRightShift(resolvedLeft, resolvedRight);
            else if(op == JavaTokenType.LTLTEQ)
                node = methodGenerator.createLeftShift(resolvedLeft, resolvedRight);
            else {
                throw new InternalException("Unsupported assignment operator " + op);
            }
            assignment = Values.node(node);
        }
        return processAssignment(
                expression.getLExpression(), assignment, context
        );
    }

    private org.metavm.flow.Value processAssignment(PsiExpression assigned, org.metavm.flow.Value assignment, ResolutionContext context) {
        if(assigned instanceof PsiReferenceExpression refExpr) {
            var target = refExpr.resolve();
            if (target instanceof PsiVariable variable) {
                if (variable instanceof PsiField psiField) {
                    if (TranspileUtils.isStatic(psiField)) {
                        var klass = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.createType(psiField.getContainingClass()))).resolve();
                        var field = klass.getStaticFieldByName(psiField.getName());
                        methodGenerator.createSetStatic(field, assignment);
                    } else {
                        org.metavm.flow.Value self;
                        if (refExpr.getQualifierExpression() != null) {
                            self = resolve(refExpr.getQualifierExpression(), context);
                            if(methodGenerator.getExpressionType(self.getExpression()).isNullable())
                                self = Values.node(methodGenerator.createNonNull("nonNull", self));
                        } else {
                            self = getThis();
                        }
                        Klass instanceType = Types.resolveKlass(methodGenerator.getExpressionType(self.getExpression()));
                        typeResolver.ensureDeclared(instanceType);
                        Field field = instanceType.getFieldByCode(psiField.getName());
                        methodGenerator.createSetField(self, field, assignment);
                    }
                } else {
                    var lambda = currentLambda();
                    int contextIndex;
                    var index = TranspileUtils.getVariableIndex(variable);
                    if(lambda != null && (contextIndex = TranspileUtils.getContextIndex(variable, lambda)) >= 0)
                        methodGenerator.createStoreContextSlot(contextIndex, index, assignment);
                    else
                        methodGenerator.createStore(index, assignment);
                }
            } else {
                throw new InternalException("Invalid assignment target " + target);
            }
            return assignment;
        }
        else if(assigned instanceof PsiArrayAccessExpression arrayAccess) {
            var array = resolve(arrayAccess.getArrayExpression(), context);
            if(array.getType().isNullable())
                array = Values.node(methodGenerator.createNonNull("nonNull", array));
            var index = resolve(arrayAccess.getIndexExpression(), context);
            if(index.getType().isNullable())
                index = Values.node(methodGenerator.createNonNull("nonNull", index));
            methodGenerator.createSetElement(array, index, assignment);
            return assignment;
        }
        else
            throw new IllegalStateException("Unsupported assignment target: " + assigned);
    }

    void processChildAssignment(org.metavm.flow.Value self, @Nullable Field field, Expression assignment) {
        NncUtils.requireTrue(field == null || field.isChild());
        NncUtils.requireTrue(assignment instanceof NodeExpression);
        var node = ((NodeExpression) assignment).getNode();
        if (node instanceof NewNode newNode) {
            newNode.setParentRef(new ParentRef(self, NncUtils.get(field, Field::getRef)));
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
        var expr = resolveNormal(condition, context);
        var ifNode = negated ? methodGenerator.createIf(expr, null)
                : methodGenerator.createIfNot(expr, null);
        thenAction.run();
        var g = methodGenerator.createGoto(null);
        ifNode.setTarget(methodGenerator.createNoop());
        elseAction.run();
        var join = methodGenerator.createNoop();
        g.setTarget(join);
        return join;
    }

    public org.metavm.flow.Value resolveBoolExpr(PsiExpression expression, ResolutionContext context) {
        var i = methodGenerator.nextVariableIndex();
         constructIf(expression, false,
                () -> methodGenerator.createStore(i, Values.constantTrue()),
                () -> methodGenerator.createStore(i, Values.constantFalse()), context);
        return Values.node(methodGenerator.createLoad(i, Types.getBooleanType()));
    }

    public org.metavm.flow.Value resolveLambdaExpression(PsiLambdaExpression expression, ResolutionContext context) {
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
//        lambda.getScope().setMaxLocals(TranspileUtils.getMaxLocals(expression));
        methodGenerator.enterScope(lambda.getScope());
        if (expression.getBody() instanceof PsiExpression bodyExpr) {
            methodGenerator.createReturn(resolve(bodyExpr, context));
        } else {
            requireNonNull(expression.getBody()).accept(visitor);
            if (lambda.getReturnType().isVoid()) {
                var lastNode = methodGenerator.scope().getLastNode();
                if (lastNode == null || !lastNode.isExit())
                    methodGenerator.createReturn();
            }
        }
        methodGenerator.exitScope();
        exitLambda();
        return Values.node(methodGenerator.createLambda(lambda, funcInterface));
    }

    private org.metavm.flow.Value resolveSwitchExpression(PsiSwitchExpression psiSwitchExpression, ResolutionContext context) {
        var y = methodGenerator.enterSwitchExpression();
        var switchExpr = resolve(psiSwitchExpression.getExpression(), context);
         methodGenerator.createNoop();
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
                    ifNodes.add(resolveSwitchCaseLabel(switchExpr, element, context));
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
        return Values.node(methodGenerator.createLoad(y, type));
    }

    private IfNotNode resolveSwitchCaseLabel(org.metavm.flow.Value switchExpr, PsiElement label, ResolutionContext context) {
        if(label instanceof PsiExpression expression) {
            return methodGenerator.createIfNot(Values.node(
                    methodGenerator.createEq(switchExpr, resolve(expression, context))
            ), null);
        }
        if(label instanceof PsiTypeTestPattern typeTestPattern) {
            var checkType = requireNonNull(typeTestPattern.getCheckType()).getType();
            methodGenerator.createStore(
                    methodGenerator.getVariableIndex(Objects.requireNonNull(typeTestPattern.getPatternVariable())),
                    switchExpr
            );
            return methodGenerator.createIfNot(
                    Values.node(methodGenerator.createInstanceOf(switchExpr, typeResolver.resolveDeclaration(checkType))),
                    null
            );
        }
        throw new IllegalArgumentException("Invalid switch case: " + label);
    }

    private void processSwitchCaseBody(PsiElement caseBody, ResolutionContext context) {
        if (caseBody instanceof PsiExpressionStatement exprStmt)
            methodGenerator.setYield(resolve(exprStmt.getExpression(), context));
        else
            caseBody.accept(visitor);
    }

    private Parameter resolveParameter(PsiParameter psiParameter) {
        return new Parameter(
                null, TranspileUtils.getFlowParamName(psiParameter), psiParameter.getName(),
                typeResolver.resolveNullable(psiParameter.getType(), ResolutionStage.DECLARATION)
        );
    }

    private org.metavm.flow.Value trueValue() {
        return Values.constantTrue();
    }

    private org.metavm.flow.Value falseValue() {
        return Values.constantFalse();
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

    org.metavm.flow.Value getThis() {
        assert !methodGenerator.getMethod().isStatic();
        var lambda = currentLambda();
        if(lambda == null)
            return methodGenerator.getThis();
        else {
            return new NodeValue(methodGenerator.createLoadContextSlot(
                    TranspileUtils.getMethodContextIndex(lambda), 0, methodGenerator.getThisType())
            );
        }
    }

    public static class ResolutionContext {
        private final Map<QualifiedName, org.metavm.flow.Value> tempVariableMap = new HashMap<>();
        private final List<VariableOperation> variableOperations = new ArrayList<>();
        private final List<FieldOperation> fieldOperations = new ArrayList<>();
        private Expression result;

        public org.metavm.flow.Value get(QualifiedName qn) {
            return tempVariableMap.get(qn);
        }

        public void set(QualifiedName qn, org.metavm.flow.Value expression) {
            tempVariableMap.put(qn, expression);
        }

        public void finish(MethodGenerator flowBuilder) {
            for (FieldOperation(org.metavm.flow.Value instance, Field field, org.metavm.flow.Value value) : fieldOperations) {
                flowBuilder.createSetField(instance, field, value);
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
            org.metavm.flow.Value value
    ) {

    }

    private record FieldOperation(
            org.metavm.flow.Value instance,
            Field field,
            org.metavm.flow.Value value
    ) {

    }

}
