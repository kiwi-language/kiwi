package org.metavm.autograph;

import com.google.common.collect.Streams;
import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.BinaryOperator;
import org.metavm.flow.*;
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
import static org.metavm.autograph.TranspileUtils.createTemplateType;

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
    private final Map<PsiExpression, Node> expression2node = new IdentityHashMap<>();
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

    public Node resolve(PsiExpression psiExpression) {
        ResolutionContext context = new ResolutionContext();
        try {
            return resolve(psiExpression, context);
        }
        catch (Exception e) {
            throw new InternalException("Failed to resolve expression: " + psiExpression.getClass().getSimpleName() + " " + psiExpression.getText(), e);
        }
    }

    private Node resolve(PsiExpression psiExpression, ResolutionContext context) {
        Node resolved;
        if (isBoolExpression(psiExpression)) {
            resolved = resolveBoolExpr(psiExpression, context);
        } else {
            resolved = resolveNormal(psiExpression, context);
        }
        if(resolved != null && !expression2node.containsKey(psiExpression))
            expression2node.put(psiExpression, resolved);
        return resolved;
    }

    private Node resolveNormal(PsiExpression psiExpression, ResolutionContext context) {
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

    private Node resolveSuper(PsiSuperExpression ignored) {
        return loadThis();
    }

    private Node resolveClassObjectAccess(PsiClassObjectAccessExpression classObjectAccessExpression, ResolutionContext ignored) {
        var type = typeResolver.resolveTypeOnly(classObjectAccessExpression.getOperand().getType());
        return methodGenerator.createLoadType(type);
    }

    private Node resolveTypeCast(PsiTypeCastExpression typeCastExpression, ResolutionContext context) {
        var operand = Objects.requireNonNull(typeCastExpression.getOperand());
        var sourceType = operand.getType();
        var operandNode = resolve(operand, context);
        var castType = requireNonNull(typeCastExpression.getCastType()).getType();
        if (sourceType instanceof PsiPrimitiveType t1 && castType instanceof PsiPrimitiveType t2) {
            if (t1.equals(PsiType.DOUBLE)) {
                if (t2.equals(PsiType.FLOAT))
                    return methodGenerator.createDoubleToFloat();
                else if (t2.equals(PsiType.LONG))
                    return methodGenerator.createDoubleToLong();
                else if (t2.equals(PsiType.INT))
                    return methodGenerator.createDoubleToInt();
                else if (t2.equals(PsiType.SHORT))
                    return methodGenerator.createDoubleToShort();
                else if (t2.equals(PsiType.BYTE))
                    return methodGenerator.createDoubleToByte();
                else if (t2.equals(PsiType.CHAR))
                    return methodGenerator.createDoubleToChar();
            } else if (t1.equals(PsiType.FLOAT)) {
                if (t2.equals(PsiType.DOUBLE))
                    return methodGenerator.createFloatToDouble();
                else if (t2.equals(PsiType.LONG))
                    return methodGenerator.createFloatToLong();
                else if (t2.equals(PsiType.INT))
                    return methodGenerator.createFloatToInt();
                else if (t2.equals(PsiType.SHORT))
                    return methodGenerator.createFloatToShort();
                else if (t2.equals(PsiType.BYTE))
                    return methodGenerator.createFloatToByte();
                else if (t2.equals(PsiType.CHAR))
                    return methodGenerator.createFloatToChar();
            } else if (t1.equals(PsiType.LONG)) {
                if (t2.equals(PsiType.DOUBLE))
                    return methodGenerator.createLongToDouble();
                else if (t2.equals(PsiType.FLOAT))
                    return methodGenerator.createLongToFloat();
                else if (t2.equals(PsiType.INT))
                    return methodGenerator.createLongToInt();
                else if (t2.equals(PsiType.SHORT))
                    return methodGenerator.createLongToShort();
                else if (t2.equals(PsiType.BYTE))
                    return methodGenerator.createLongToByte();
                else if (t2.equals(PsiType.CHAR))
                    return methodGenerator.createLongToChar();
            } else if (t1.equals(PsiType.INT)) {
                if (t2.equals(PsiType.DOUBLE))
                    return methodGenerator.createIntToDouble();
                else if (t2.equals(PsiType.FLOAT))
                    return methodGenerator.createIntToFloat();
                else if (t2.equals(PsiType.LONG))
                    return methodGenerator.createIntToLong();
                else if (t2.equals(PsiType.SHORT))
                    return methodGenerator.createIntToShort();
                else if (t2.equals(PsiType.BYTE))
                    return methodGenerator.createIntToByte();
                else if (t2.equals(PsiType.CHAR))
                    return methodGenerator.createIntToChar();
            } else if (t1.equals(PsiType.SHORT)) {
                if (t2.equals(PsiType.DOUBLE))
                    return methodGenerator.createIntToDouble();
                else if (t2.equals(PsiType.FLOAT))
                    return methodGenerator.createIntToFloat();
                else if (t2.equals(PsiType.LONG))
                    return methodGenerator.createIntToLong();
                else if (t2.equals(PsiType.INT))
                    return operandNode;
                else if (t2.equals(PsiType.BYTE))
                    return methodGenerator.createIntToByte();
                else if (t2.equals(PsiType.CHAR))
                    return methodGenerator.createIntToChar();
            } else if (t1.equals(PsiType.BYTE)) {
                if (t2.equals(PsiType.DOUBLE))
                    return methodGenerator.createIntToDouble();
                else if (t2.equals(PsiType.FLOAT))
                    return methodGenerator.createIntToFloat();
                else if (t2.equals(PsiType.LONG))
                    return methodGenerator.createIntToLong();
                else if (t2.equals(PsiType.INT))
                    return operandNode;
                else if (t2.equals(PsiType.SHORT))
                    return methodGenerator.createIntToShort();
                else if (t2.equals(PsiType.CHAR))
                    return methodGenerator.createIntToChar();
            } else if (t1.equals(PsiType.CHAR)) {
                if (t2.equals(PsiType.DOUBLE))
                    return methodGenerator.createIntToDouble();
                else if (t2.equals(PsiType.FLOAT))
                    return methodGenerator.createIntToFloat();
                else if (t2.equals(PsiType.LONG))
                    return methodGenerator.createIntToLong();
                else if (t2.equals(PsiType.INT))
                    return operandNode;
                else if (t2.equals(PsiType.SHORT))
                    return methodGenerator.createIntToShort();
                else if (t2.equals(PsiType.BYTE))
                    return methodGenerator.createIntToByte();
            }
            throw new InternalException("Unrecognized primitive type cast:" + typeCastExpression.getText());
        }
        else
            return methodGenerator.createTypeCast(typeResolver.resolveDeclaration(castType));
    }

    private Node resolvePolyadic(PsiPolyadicExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationTokenType();
        var operands = psiExpression.getOperands();
        var current = resolve(operands[0], context);
        for (int i = 1; i < operands.length; i++) {
            resolve(operands[i]);
            current = resolveBinary(op, psiExpression.getType());
        }
        return current;
    }

    private Node resolveInstanceOf(PsiInstanceOfExpression instanceOfExpression, ResolutionContext context) {
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

    private Node resolveArrayAccess(PsiArrayAccessExpression arrayAccessExpression, ResolutionContext context) {
        resolve(arrayAccessExpression.getArrayExpression(), context);
        Values.node(methodGenerator.createNonNull());
        resolve(arrayAccessExpression.getIndexExpression(), context);
        Values.node(methodGenerator.createNonNull());
        return methodGenerator.createGetElement();
    }

    public TypeResolver getTypeResolver() {
        return typeResolver;
    }

    public VisitorBase getVisitor() {
        return visitor;
    }

    private boolean isBoolExpression(PsiExpression psiExpression) {
        return psiExpression.getType() == PsiType.BOOLEAN;
    }

    private Node resolveThis(PsiThisExpression expression) {
        var qualifier = expression.getQualifier();
        var targetKlass = qualifier != null ? (PsiClass) requireNonNull(qualifier.resolve())
                : TranspileUtils.getParent(expression, PsiClass.class);
        PsiElement e = expression;
        int cIdx = -2;
        int pIdx = -1;
        PsiMethod enclosingMethod = null;
        while (e != null && e != targetKlass) {
            switch (e) {
                case PsiLambdaExpression ignored -> cIdx++;
                case PsiMethod psiMethod -> {
                    NncUtils.requireFalse(TranspileUtils.isStatic(psiMethod),
                            () -> "Encountered static method " + psiMethod.getName() + " on the path to class " + targetKlass.getName());
                    cIdx++;
                    pIdx = -1;
                    enclosingMethod = psiMethod;
                }
                case PsiClass psiClass -> {
                    NncUtils.requireFalse(TranspileUtils.isStatic(psiClass),
                            () -> "Encountered static class " + psiClass.getName() + " on the path to class " + targetKlass.getName());
                    pIdx++;
                }
                default -> {
                }
            }
            e = e.getParent();
        }
        requireNonNull(e, () -> targetKlass.getName() + " is not an enclosing class of expression " + expression.getText());
        Node result;
        if (cIdx >= 0) {
            result = methodGenerator.createLoadContextSlot(cIdx, 0,
                    typeResolver.resolveDeclaration(createTemplateType(
                            requireNonNull(requireNonNull(enclosingMethod).getContainingClass()))
                    ));
        } else
            result = methodGenerator.createLoadThis();
        if (pIdx >= 0) {
            methodGenerator.createLoadParent(pIdx);
            result = methodGenerator.createTypeCast(typeResolver.resolveDeclaration(expression.getType()));
        }
        return result;
    }

    private Node resolveConditional(PsiConditionalExpression psiExpression, ResolutionContext context) {
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

    private Node resolveParenthesized(PsiParenthesizedExpression psiExpression, ResolutionContext context) {
        return resolve(requireNonNull(psiExpression.getExpression()), context);
    }

    private Node resolveLiteral(PsiLiteralExpression literalExpression) {
        Object value = literalExpression.getValue();
        var instance = switch (value) {
            case null -> Instances.nullInstance();
            case Boolean boolValue -> Instances.intInstance(boolValue);
            case Integer integer -> Instances.intInstance(integer);
            case Long longValue -> Instances.longInstance(longValue);
            case Float floatValue -> Instances.floatInstance(floatValue);
            case Double doubleValue -> Instances.doubleInstance(doubleValue);
            case Character c -> Instances.intInstance(c);
            case String string -> Instances.stringInstance(string);
            default -> throw new InternalException("Unrecognized literal: " + value);
        };
        return methodGenerator.createLoadConstant(instance);
    }

    private Node resolveReference(PsiReferenceExpression psiReferenceExpression, ResolutionContext context) {
        var target = psiReferenceExpression.resolve();
        switch (target) {
            case PsiField psiField -> {
                if (isArrayLength(psiField)) {
                    resolveQualifier(psiReferenceExpression.getQualifierExpression(), context);
                    return methodGenerator.createArrayLength();
                } else {
                    PsiClass psiClass = requireNonNull(psiField.getContainingClass());
                    if (psiField.hasModifierProperty(PsiModifier.STATIC)) {
                        var className = psiField.getContainingClass().getQualifiedName();
                        if (className != null && className.startsWith("java.")) {
                            var javaField = ReflectionUtils.getField(ReflectionUtils.classForName(className), psiField.getName());
                            if (PrimitiveStaticFields.isConstant(javaField))
                                return methodGenerator.createLoadConstant(Instances.fromConstant(PrimitiveStaticFields.getConstant(javaField)));
                        }
                        var type = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass)));
                        var field = Objects.requireNonNull(type.getKlass().findSelfStaticFieldByName(psiField.getName()));
                        return methodGenerator.createGetStatic(new FieldRef(type, field));
                    } else {
                        var qualifierExpr = psiReferenceExpression.getQualifierExpression();
                        resolveQualifier(qualifierExpr, context);
                        methodGenerator.createNonNull();
                        var generics = psiReferenceExpression.advancedResolve(false);
                        var type = (ClassType) typeResolver.resolveDeclaration(
                                generics.getSubstitutor().substitute(createTemplateType(psiClass))
                        );
                        var field = type.getKlass().getSelfFieldByName(psiField.getName());
                        return methodGenerator.createGetProperty(new FieldRef(type, field));
                    }
                }
            }
            case PsiMethod psiMethod -> {
                var methodRefExpr = (PsiMethodReferenceExpression) psiReferenceExpression;
                var type = (ClassType) typeResolver.resolveDeclaration(methodRefExpr.getFunctionalInterfaceType());
                PsiClass psiClass = requireNonNull(psiMethod.getContainingClass());
                if (psiMethod.hasModifierProperty(PsiModifier.STATIC)) {
                    var classType = (ClassType) typeResolver.resolveDeclaration(TranspileUtils.getRawType(psiClass));
                    var method = classType.getKlass().getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                    methodGenerator.createGetStatic(new MethodRef(classType, method, List.of()));
                } else {
                    if (psiReferenceExpression.getQualifierExpression() instanceof PsiReferenceExpression refExpr &&
                            refExpr.resolve() instanceof PsiClass) {
                        var classType = (ClassType) typeResolver.resolve(TranspileUtils.createType(psiClass));
                        var method = classType.getKlass().getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                        methodGenerator.createGetStatic(new MethodRef(classType, method, List.of()));
                    } else {
                        var qualifierExpr = Objects.requireNonNull(psiReferenceExpression.getQualifierExpression());
                        var classType = (ClassType) typeResolver.resolve(qualifierExpr.getType());
                        var method = classType.getKlass().getMethodByInternalName(TranspileUtils.getInternalName(psiMethod));
                        resolveQualifier(qualifierExpr, context);
                        methodGenerator.createGetProperty(new MethodRef(classType, method, List.of()));
                    }
                }
                return createSAMConversion(type);
            }
            case PsiVariable variable -> {
                int contextIndex;
                var type = typeResolver.resolveNullable(variable.getType(), ResolutionStage.DECLARATION);
                var index = TranspileUtils.getVariableIndex(variable);
                if ((contextIndex = TranspileUtils.getContextIndex(variable, psiReferenceExpression)) >= 0)
                    return methodGenerator.createLoadContextSlot(contextIndex, index, type);
                else
                    return methodGenerator.createLoad(index, type);
            }
            case null, default -> {
                throw new InternalException("Can not resolve reference expression "
                        + psiReferenceExpression.getText() + " with target: " + target);
            }
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private Node resolveQualifier(PsiExpression qualifier, ResolutionContext context) {
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

    private Node resolvePrefix(PsiPrefixExpression psiPrefixExpression, ResolutionContext context) {
        var operand = requireNonNull(psiPrefixExpression.getOperand());
        var op = psiPrefixExpression.getOperationSign().getTokenType();
        var type = psiPrefixExpression.getType();
        if (op == JavaTokenType.EXCL) {
            resolve(operand, context);
            return methodGenerator.createNe();
        } else if(op == JavaTokenType.MINUS) {
            resolve(operand, context);
            return methodGenerator.createNeg(type);
        } else if(op == JavaTokenType.PLUS) {
            return resolve(operand, context);
        } else if(op == JavaTokenType.TILDE) {
            resolve(operand, context);
            return methodGenerator.createBitNot(type);
        } else if (op == JavaTokenType.PLUSPLUS) {
            return resolveCompoundAssignment(operand, node -> methodGenerator.createInc(type), () -> {}, context);
        } else if (op == JavaTokenType.MINUSMINUS) {
            return resolveCompoundAssignment(operand, node -> methodGenerator.createDec(type), () -> {}, context);
        } else {
            throw new InternalException("Unsupported prefix operator " + op);
        }
    }

    private Node resolveUnary(PsiUnaryExpression psiUnaryExpression, ResolutionContext context) {
        if (psiUnaryExpression instanceof PsiPrefixExpression prefixExpression) {
            return resolvePrefix(prefixExpression, context);
        }
        var op = psiUnaryExpression.getOperationSign().getTokenType();
        var type = psiUnaryExpression.getType();
        if (op == JavaTokenType.PLUSPLUS) {
            return resolveCompoundAssignment(
                    psiUnaryExpression.getOperand(),
                    node -> node,
                    () -> methodGenerator.createInc(type),
                    context
            );
        } else if (op == JavaTokenType.MINUSMINUS) {
            return resolveCompoundAssignment(
                    psiUnaryExpression.getOperand(),
                    node -> node,
                    () -> methodGenerator.createDec(type),
                    context
            );
        } else
            throw new IllegalStateException("Unrecognized unary expression: " + psiUnaryExpression.getText());
    }

    private Node resolveBinary(PsiBinaryExpression psiExpression, ResolutionContext context) {
        var op = psiExpression.getOperationSign().getTokenType();
        resolve(psiExpression.getLOperand(), context);
        resolve(psiExpression.getROperand(), context);
        return resolveBinary(op, psiExpression.getLOperand().getType());
    }

    private Node resolveBinary(IElementType op, PsiType type) {
        Node node;
        if(op.equals(JavaTokenType.PLUS))
            node = methodGenerator.createAdd(type);
        else if(op.equals(JavaTokenType.MINUS))
            node = methodGenerator.createSub(type);
        else if(op.equals(JavaTokenType.ASTERISK))
            node = methodGenerator.createMul(type);
        else if(op.equals(JavaTokenType.DIV))
            node = methodGenerator.createDiv(type);
        else if(op.equals(JavaTokenType.LTLT))
            node = methodGenerator.createShiftLeft(type);
        else if(op.equals(JavaTokenType.GTGT))
            node = methodGenerator.createShiftRight(type);
        else if(op.equals(JavaTokenType.GTGTGT))
            node = methodGenerator.createUnsignedShiftRight(type);
        else if(op.equals(JavaTokenType.OR))
            node = methodGenerator.createBitOr(type);
        else if(op.equals(JavaTokenType.AND))
            node = methodGenerator.createBitAnd(type);
        else if(op.equals(JavaTokenType.XOR))
            node = methodGenerator.createBitXor(type);
        else if(op.equals(JavaTokenType.ANDAND))
            node = methodGenerator.createIntBitAnd();
        else if(op.equals(JavaTokenType.OROR))
            node = methodGenerator.createIntBitOr();
        else if(op.equals(JavaTokenType.PERC))
            node = methodGenerator.createRem(type);
        else if(op.equals(JavaTokenType.EQEQ))
            node = methodGenerator.createCompareEq(type);
        else if(op.equals(JavaTokenType.NE))
            node = methodGenerator.createCompareNe(type);
        else if(op.equals(JavaTokenType.GE))
            node = methodGenerator.createCompareGe(type);
        else if(op.equals(JavaTokenType.GT))
            node = methodGenerator.createCompareGt(type);
        else if(op.equals(JavaTokenType.LT))
            node = methodGenerator.createCompareLt(type);
        else if(op.equals(JavaTokenType.LE))
            node = methodGenerator.createCompareLe(type);
        else
            throw new IllegalStateException("Unrecognized operator " + op);
        return node;
    }

    private Node resolveMethodCall(PsiMethodCallExpression expression, ResolutionContext context) {
        var methodCallResolver = getMethodCallResolver(expression);
        if (methodCallResolver != null)
            return methodCallResolver.resolve(expression, this, methodGenerator);
        return resolveFlowCall(expression, context);
    }

    @Nullable
    private MethodCallResolver getMethodCallResolver(PsiMethodCallExpression methodCallExpression) {
        var methodExpr = methodCallExpression.getMethodExpression();
        var qualifier = methodExpr.getQualifierExpression();
        var method = (PsiMethod) Objects.requireNonNull(methodExpr.resolve(),
                () -> "Failed to resolve method expression for " + methodCallExpression.getText());
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

    private Node resolveFlowCall(PsiMethodCallExpression expression, ResolutionContext context) {
        var ref = expression.getMethodExpression();
        var rawMethod = (PsiMethod) requireNonNull(ref.resolve());
        var klassName = requireNonNull(rawMethod.getContainingClass()).getQualifiedName();
        if (klassName != null && klassName.startsWith("org.metavm.lang.")) {
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
        var methodGenerics = expression.resolveMethodGenerics();
        var substitutor = methodGenerics.getSubstitutor();
        var psiMethod = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var type = (ClassType) typeResolver.resolveDeclaration(
                substitutor.substitute(createTemplateType(requireNonNull(psiMethod.getContainingClass())))
        );
        List<Type> paramTypes = NncUtils.map(
                psiMethod.getParameterList().getParameters(),
                param -> typeResolver.resolveNullable(param.getType(), ResolutionStage.DECLARATION)
        );
        var method = type.getKlass().getSelfMethod(
                m -> m.getName().equals(psiMethod.getName()) && m.getParameterTypes().equals(paramTypes)
        );
        var typeArgs = NncUtils.map(psiMethod.getTypeParameters(),
                tp -> typeResolver.resolveDeclaration(substitutor.substitute(tp)));
        var psiArgs = expression.getArgumentList().getExpressions();
        for (var psiArg : psiArgs) {
             resolve(psiArg, context);
        }
        var node = methodGenerator.createMethodCall(new MethodRef(type, method, typeArgs));
        setCapturedVariables(node);
        return node;
    }

    void setCapturedVariables(CallNode node) {
        var flow = node.getFlowRef();
        var capturedTypeSet = new HashSet<CapturedType>();
        if (flow instanceof MethodRef methodRef) {
            var declaringType = methodRef.getDeclaringType();
            if(declaringType.isParameterized())
                declaringType.getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        }
        if(flow.isParameterized())
            flow.getTypeArguments().forEach(t -> t.getCapturedTypes(capturedTypeSet));
        var capturedTypes = new ArrayList<>(capturedTypeSet);
        var psiCapturedTypes = NncUtils.map(capturedTypes, typeResolver::getPsiCapturedType);
        var capturedPsiExpressions = NncUtils.mapAndFilterByType(psiCapturedTypes, PsiCapturedWildcardType::getContext, PsiExpression.class);
        var anchors = NncUtils.map(capturedPsiExpressions,
                e -> Objects.requireNonNull(expression2node.get(e),
                        () -> "Captured expression '" + e.getText() + "' has not yet been resolved"));
        var captureVariableTypes = NncUtils.map(
                capturedPsiExpressions, e -> typeResolver.resolveDeclaration(e.getType()));
        var capturedVariableIndexes = new ArrayList<Integer>();
        for (Node anchor : anchors) {
            var v = methodGenerator.nextVariableIndex();
            capturedVariableIndexes.add(v);
            methodGenerator.recordValue(anchor, v);
        }
        node.setCapturedVariableIndexes(capturedVariableIndexes);
        node.setCapturedVariableTypes(captureVariableTypes);
    }

    private Node createSAMConversion(ClassType samInterface) {
        var funcRef = new FunctionRef(StdFunction.functionToInstance.get(), List.of(samInterface));
        return methodGenerator.createFunctionCall(funcRef);
    }

    @SuppressWarnings("UnusedReturnValue")
    private Node getQualifier(@Nullable PsiExpression qualifierExpression, ResolutionContext context) {
        return qualifierExpression == null ? loadThis()
                : resolve(requireNonNull(qualifierExpression), context);
    }

    private Node resolveNew(PsiNewExpression expression, ResolutionContext context) {
        if (expression.isArrayCreation()) {
            return resolveNewArray(expression, context);
        } else {
            return resolveNewPojo(expression, context);
        }
    }

    private Node resolveNewArray(PsiNewExpression expression, ResolutionContext context) {
        var type = (ArrayType) typeResolver.resolveDeclaration(expression.getType());
        Node node;
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

    private Node resolveArrayInitialization(PsiArrayInitializerExpression arrayInitializerExpression, ResolutionContext context) {
        var type = (ArrayType) typeResolver.resolveTypeOnly(arrayInitializerExpression.getType());
        var node = methodGenerator.createNewArray(type);
        for (PsiExpression initializer : arrayInitializerExpression.getInitializers()) {
            methodGenerator.createDup();
            resolve(initializer, context);
            methodGenerator.createAddElement();
        }
        return node;
    }

    private Node resolveNewPojo(PsiNewExpression expression, ResolutionContext context) {
        var resolver = getNewResolver(expression);
        if (resolver != null)
            return resolver.resolve(expression, this, methodGenerator);
        if (expression.getType() instanceof PsiClassType psiClassType) {
            var type = (ClassType) typeResolver.resolve(psiClassType);
            NncUtils.map(
                    requireNonNull(expression.getArgumentList()).getExpressions(),
                    expr -> resolve(expr, context)
            );
            var methodGenerics = expression.resolveMethodGenerics();
            var qualifier = expression.getQualifier();
            if(qualifier != null)
                resolve(qualifier, context);
            var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
            var paramTypes = NncUtils.map(method.getParameterList().getParameters(),
                    p -> typeResolver.resolveNullable(p.getType(), ResolutionStage.DECLARATION));
            var klass = type.getKlass();
            var flow = klass.getMethod(m -> m.isConstructor() && m.getParameterTypes().equals(paramTypes));
            var typeArgs = NncUtils.map(
                    method.getTypeParameters(),
                    tp -> typeResolver.resolveDeclaration(methodGenerics.getSubstitutor().substitute(tp))
            );
            var methodRef = new MethodRef(type, flow, typeArgs);
            var node = qualifier == null ?
                    methodGenerator.createNew(methodRef, false, false) :
                    methodGenerator.createNewChild(methodRef);
            setCapturedVariables(node);
            return node;
        } else {
            // TODO support new array instance
            throw new InternalException("Unsupported NewExpression: " + expression);
        }
    }

    private Node resolveAssignment(PsiAssignmentExpression expression, ResolutionContext context) {
        var op = expression.getOperationSign().getTokenType();
        var type = expression.getType();
        if (op == JavaTokenType.EQ)
            return resolveDirectAssignment(expression.getLExpression(), expression.getRExpression(), context);
        else {
            return resolveCompoundAssignment(
                    expression.getLExpression(),
                    node -> {
                        resolve(requireNonNull(expression.getRExpression()), context);
                        if (op == JavaTokenType.PLUSEQ) {
                            return methodGenerator.createAdd(type);
                        } else if (op == JavaTokenType.MINUSEQ) {
                            return methodGenerator.createSub(type);
                        } else if (op == JavaTokenType.ASTERISKEQ) {
                            return methodGenerator.createMul(type);
                        } else if (op == JavaTokenType.DIVEQ) {
                            return methodGenerator.createDiv(type);
                        } else if (op == JavaTokenType.OREQ) {
                            return methodGenerator.createBitOr(type);
                        } else if (op == JavaTokenType.ANDEQ) {
                            return methodGenerator.createBitAnd(type);
                        } else if (op == JavaTokenType.XOREQ) {
                            return methodGenerator.createBitXor(type);
                        } else if (op == JavaTokenType.GTGTGTEQ)
                            return methodGenerator.createUnsignedShiftRight(type);
                        else if (op == JavaTokenType.GTGTEQ)
                            return methodGenerator.createShiftRight(type);
                        else if (op == JavaTokenType.LTLTEQ)
                            return methodGenerator.createShiftLeft(type);
                        else {
                            throw new InternalException("Unsupported assignment operator " + op);
                        }
                    },
                    () -> {},
                    context
            );
        }
    }

    private Node resolveDirectAssignment(PsiExpression assigned, PsiExpression assignment, ResolutionContext context) {
        if(assigned instanceof PsiReferenceExpression refExpr) {
            var generics = refExpr.advancedResolve(false);
            var target = generics.getElement();
            Node node;
            if (target instanceof PsiVariable variable) {
                if (variable instanceof PsiField psiField) {
                    if (TranspileUtils.isStatic(psiField)) {
                        var classType = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.createType(psiField.getContainingClass())));
                        var field = classType.getKlass().getSelfStaticFieldByName(psiField.getName());
                        node = resolve(assignment, context);
                        methodGenerator.createDup();
                        methodGenerator.createSetStatic(new FieldRef(classType, field));
                    } else {
                        if (refExpr.getQualifierExpression() != null) {
                            resolve(refExpr.getQualifierExpression(), context);
                            methodGenerator.createNonNull();
                        } else
                            loadThis();
                        var type = (ClassType) typeResolver.resolveDeclaration(
                                generics.getSubstitutor().substitute(
                                        createTemplateType(requireNonNull(psiField.getContainingClass()))
                                )
                        );
                        var field = type.getKlass().getSelfFieldByName(psiField.getName());
                        node = resolve(assignment, context);
                        methodGenerator.createDupX1();
                        methodGenerator.createSetField(new FieldRef(type, field));
                    }
                } else {
                    int contextIndex;
                    var index = TranspileUtils.getVariableIndex(variable);
                    node = resolve(assignment, context);
                    methodGenerator.createDup();
                    if((contextIndex = TranspileUtils.getContextIndex(variable, assigned)) >= 0)
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

    private Node resolveCompoundAssignment(PsiExpression operand,
                                           Function<Node, Node> action1,
                                           Runnable action2,
                                           ResolutionContext context) {
        if(operand instanceof PsiReferenceExpression refExpr) {
            var generics = refExpr.advancedResolve(false);
            var target = generics.getElement();
            Node assignment;
            if (target instanceof PsiVariable variable) {
                if (variable instanceof PsiField psiField) {
                    if (TranspileUtils.isStatic(psiField)) {
                        var classType = ((ClassType) typeResolver.resolveDeclaration(TranspileUtils.createType(psiField.getContainingClass())));
                        var field = classType.getKlass().getSelfStaticFieldByName(psiField.getName());
                        assignment = methodGenerator.createGetStatic(new FieldRef(classType, field));
                        assignment = action1.apply(assignment);
                        methodGenerator.createDup();
                        action2.run();
                        methodGenerator.createSetStatic(new FieldRef(classType, field));
                    } else {
                        if (refExpr.getQualifierExpression() != null) {
                            resolve(refExpr.getQualifierExpression(), context);
                            methodGenerator.createNonNull();
                        } else
                            loadThis();
                        methodGenerator.createDup();
                        var type = (ClassType) typeResolver.resolveDeclaration(
                                generics.getSubstitutor().substitute(
                                        createTemplateType(requireNonNull(psiField.getContainingClass()))
                                )
                        );
                        var field = type.getKlass().getSelfFieldByName(psiField.getName());
                        assignment = methodGenerator.createGetProperty(new FieldRef(type, field));
                        assignment = action1.apply(assignment);
                        methodGenerator.createDupX1();
                        action2.run();
                        methodGenerator.createSetField(new FieldRef(type, field));
                    }
                } else {
                    int contextIndex;
                    var captured = (contextIndex = TranspileUtils.getContextIndex(variable, operand)) >= 0;
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
            Node assignment = methodGenerator.createGetElement();
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
    public Node constructIf(PsiExpression condition, Runnable thenAction, Runnable elseAction) {
        return constructIf(condition, false, thenAction, elseAction, new ResolutionContext());
    }

    public Node constructIf(PsiExpression condition, boolean negated, Runnable thenAction, Runnable elseAction, ResolutionContext context) {
        return switch (condition) {
            case PsiBinaryExpression binaryExpression ->
                    constructBinaryIf(binaryExpression, negated, thenAction, elseAction, context);
            case PsiUnaryExpression unaryExpression -> constructUnaryIf(unaryExpression, negated, thenAction, elseAction, context);
            case PsiPolyadicExpression polyadicExpression ->
                    constructPolyadicIf(polyadicExpression, negated, thenAction, elseAction, context);
            default -> constructAtomicIf(condition, negated, thenAction, elseAction, context);
        };
    }

    private Node constructUnaryIf(PsiUnaryExpression unaryExpression,
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

    private Node constructBinaryIf(PsiBinaryExpression condition, boolean negated, Runnable thenAction, Runnable elseAction, ResolutionContext context) {
        var op = resolveOperator(condition.getOperationSign());
        if(op == BinaryOperator.AND || op == BinaryOperator.OR)
            return constructPolyadicIf(condition, negated, thenAction, elseAction, context);
        else
            return constructAtomicIf(condition, negated, thenAction, elseAction, context);
    }

    private Node constructPolyadicIf(PsiPolyadicExpression condition, boolean negated, Runnable thenAction, Runnable elseAction, ResolutionContext context) {
        var op = resolveOperator(condition.getOperationTokenType());
        if (op == BinaryOperator.AND) {
            return constructAndPolyadicIf(condition.getOperands(),  0, negated, thenAction, elseAction, context);
        } else if (op == BinaryOperator.OR) {
            return constructOrPolyadicIf(condition.getOperands(),  0, negated, thenAction, elseAction, context);
        } else {
            throw new InternalException("Invalid operator for polyadic boolean expression: " + op);
        }
    }

    private Node constructAndPolyadicIf(PsiExpression[] items,
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

    private Node constructOrPolyadicIf(PsiExpression[] items,
                                       int index,
                                       boolean negated,
                                       Runnable thenAction, Runnable elseAction,
                                       ResolutionContext context
    ) {
        if(index == items.length - 1)
            return constructIf(items[index], negated, thenAction, elseAction, context);
        else {
            var ref = new Object() {
                Node target;
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

    private Node constructAtomicIf(PsiExpression condition, boolean negated, Runnable thenAction,
                                   Runnable elseAction, ResolutionContext context) {
        resolveNormal(condition, context);
        var ifNode = negated ? methodGenerator.createIfNe(null)
                : methodGenerator.createIfEq(null);
        thenAction.run();
        var g = methodGenerator.createGoto(null);
        ifNode.setTarget(methodGenerator.createNoop());
        elseAction.run();
        var join = methodGenerator.createNoop();
        g.setTarget(join);
        return join;
    }

    public Node resolveBoolExpr(PsiExpression expression, ResolutionContext context) {
        var i = methodGenerator.nextVariableIndex();
         constructIf(expression, false,
                () -> {
                    methodGenerator.createLoadConstant(Instances.one());
                    methodGenerator.createStore(i);
                },
                () -> {
                    methodGenerator.createLoadConstant(Instances.zero());
                    methodGenerator.createStore(i);
                }, context);
        return methodGenerator.createLoad(i, Types.getBooleanType());
    }

    public Node resolveLambdaExpression(PsiLambdaExpression expression, ResolutionContext context) {
        enterLambda(expression);
        var returnType = typeResolver.resolveNullable(TranspileUtils.getLambdaReturnType(expression), ResolutionStage.DECLARATION);
        var funcInterface = (ClassType) typeResolver.resolveDeclaration(expression.getFunctionalInterfaceType());
        var lambda = new Lambda(null, List.of(), returnType, methodGenerator.getMethod());
        var parameters = new ArrayList<Parameter>();
        int i = 0;
        for (var psiParameter : expression.getParameterList().getParameters()) {
            parameters.add(resolveParameter(psiParameter, lambda));
            psiParameter.putUserData(Keys.VARIABLE_INDEX, i++);
        }
        lambda.setParameters(parameters);
        methodGenerator.enterScope(lambda.getCode());
        if (expression.getBody() instanceof PsiExpression bodyExpr) {
            resolve(bodyExpr, context);
            if(returnType.isVoid())
                methodGenerator.createVoidReturn();
            else
                methodGenerator.createReturn();
        } else {
            requireNonNull(expression.getBody()).accept(visitor);
            if (lambda.getReturnType().isVoid()) {
                var lastNode = methodGenerator.code().getLastNode();
                if (lastNode == null || !lastNode.isExit())
                    methodGenerator.createVoidReturn();
            }
        }
        methodGenerator.exitScope();
        exitLambda();
        return methodGenerator.createLambda(lambda, funcInterface);
    }

    private Node resolveSwitchExpression(PsiSwitchExpression expression, ResolutionContext context) {
        methodGenerator.enterSwitchExpression();
        var exit = methodGenerator.createSwitch(expression);
        methodGenerator.exitSwitchExpression().connectYields(exit);
        return exit;
    }

    private Parameter resolveParameter(PsiParameter psiParameter, Callable callable) {
        return new Parameter(
                null, psiParameter.getName(),
                typeResolver.resolveNullable(psiParameter.getType(), ResolutionStage.DECLARATION),
                callable
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

    Node loadThis() {
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
