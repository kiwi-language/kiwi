package org.metavm.expression;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.ModelDefRegistry;
import org.metavm.flow.NodeRT;
import org.metavm.object.type.NeverType;
import org.metavm.object.type.Type;
import org.metavm.object.type.UnionType;
import org.metavm.util.NncUtils;
import org.metavm.util.Null;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

@Slf4j
public class TypeNarrower {

    private final Function<Expression,Type> getTypeFunc;

    public TypeNarrower(Function<Expression, Type> getTypeFunc) {
        this.getTypeFunc = getTypeFunc;
    }

    public ExpressionTypeMap narrowType(Expression expression) {
        return new ExpressionTypeMap(process(expression, false));
    }

    private Map<Expression, Type> process(Expression expression, boolean negated) {
        return switch (expression) {
            case NodeExpression nodeExpression -> processNode(nodeExpression.getNode(), negated);
            case InstanceOfExpression instanceOfExpr ->
                    processInstanceOf(instanceOfExpr.getOperand(), instanceOfExpr.getTargetType(), negated);
            case BinaryExpression binaryExpression -> processBinary(binaryExpression.getLeft(),
                    binaryExpression.getRight(),
                    binaryExpression.getOperator(),
                    negated);
            case UnaryExpression unaryExpression -> processUnary(unaryExpression, negated);
            default -> Map.of();
        };
    }

    private Map<Expression, Type> processNode(NodeRT node, boolean negated) {
//        return switch (node) {
//            case NotNode notNode -> process(notNode.getOperand().getExpression(), !negated);
//            case InstanceOfNode instanceOfNode -> processInstanceOf(
//                    instanceOfNode.getOperand().getExpression(),
//                    instanceOfNode.getTargetType(),
//                    negated);
//            case AndNode andNode -> processBinary(
//                    andNode.getFirst().getExpression(),
//                    andNode.getSecond().getExpression(),
//                    BinaryOperator.AND,
//                    negated);
//            case OrNode orNode -> processBinary(
//                    orNode.getFirst().getExpression(),
//                    orNode.getSecond().getExpression(),
//                    BinaryOperator.OR,
//                    negated);
//            case EqNode eqNode -> processBinary(
//                    eqNode.getFirst().getExpression(),
//                    eqNode.getSecond().getExpression(),
//                    BinaryOperator.EQ,
//                    negated);
//            case NeNode neNode -> processBinary(
//                    neNode.getFirst().getExpression(),
//                    neNode.getSecond().getExpression(),
//                    BinaryOperator.NE,
//                    negated);
//            default -> Map.of();
//        };
        return Map.of();
    }

    private Map<Expression, Type> processInstanceOf(Expression operand, Type targetType, boolean negated) {
        var type = negated ? typeDiff(getType(operand), targetType) :
                typeIntersection(getType(operand), targetType);
        return type != null ? Map.of(operand, type) : Map.of();
    }

    private Map<Expression, Type> processBinary(Expression first,
                                                Expression second,
                                                BinaryOperator op,
                                                boolean negated) {
        final Map<Expression, Type> result = new HashMap<>();
        if (op == BinaryOperator.AND || op == BinaryOperator.OR) {
            if (negated) {
                op = op == BinaryOperator.AND ? BinaryOperator.OR : BinaryOperator.AND;
            }
            var firstResult = process(first, negated);
            var secondResult = process(second, negated);
            if (op == BinaryOperator.AND) {
                result.putAll(mergeResults(firstResult, secondResult));
            } else {
                firstResult.forEach((expr, type) -> {
                    var secondType = secondResult.get(expr);
                    if (secondType != null) {
                        result.put(expr, typeUnion(type, secondType));
                    }
                });
            }
        } else if (op == BinaryOperator.EQ || op == BinaryOperator.NE) {
            if (negated) {
                op = op == BinaryOperator.NE ? BinaryOperator.EQ : BinaryOperator.NE;
            }
            if (op == BinaryOperator.EQ) {
                var intersection = typeIntersection(getType(first), getType(second));
                if (intersection != null) {
                    if (Expressions.isNotConstant(first)) {
                        result.put(first, intersection);
                    }
                    if (Expressions.isNotConstant(second)) {
                        result.put(second, intersection);
                    }
                }
            }
            else {
                if (Expressions.isNotConstant(first) && isSingleValuedType(second.getType())) {
                    result.put(first, typeDiff(getType(first), getType(second)));
                }
                if (Expressions.isNotConstant(second) && isSingleValuedType(first.getType())) {
                    result.put(second, typeDiff(getType(second), getType(first)));
                }
            }
        }
        return result;
    }

    private boolean isSingleValuedType(Type type) {
        return type.isNull();
    }

    public static Map<Expression, Type> mergeResults(Map<Expression, Type> firstResult, Map<Expression, Type> secondResult) {
        var result = new HashMap<Expression, Type>();
        firstResult.forEach((expr, type) -> {
            var secondType = secondResult.get(expr);
            if (secondType != null) {
                result.put(expr, typeIntersection(type, secondType));
            } else {
                result.put(expr, type);
            }
        });
        secondResult.forEach((expr, type) -> {
            if (!firstResult.containsKey(expr)) {
                result.put(expr, type);
            }
        });
        return result;
    }

    public static Map<Expression, Type> unionResults(Map<Expression, Type> firstResult, Map<Expression, Type> secondResult) {
        var result = new HashMap<Expression, Type>();
        firstResult.forEach((expr, type) -> {
            var secondType = secondResult.get(expr);
            if (secondType != null) {
                result.put(expr, typeUnion(type, secondType));
            }
        });
        return result;
    }

    private Map<Expression, Type> processUnary(UnaryExpression unaryExpression, boolean negated) {
        var operand = unaryExpression.getOperand();
        var op = unaryExpression.getOperator();
        if (op == UnaryOperator.IS_NOT_NULL || op == UnaryOperator.IS_NULL) {
            if (negated) {
                op = op == UnaryOperator.IS_NULL ? UnaryOperator.IS_NOT_NULL : UnaryOperator.IS_NULL;
            }
            if (op == UnaryOperator.IS_NOT_NULL) {
                return Map.of(operand, getType(operand).getUnderlyingType());
            } else {
                return Map.of(operand, ModelDefRegistry.getType(Null.class));
            }
        } else if (op == UnaryOperator.NOT) {
            return process(operand, !negated);
        } else {
            return Map.of();
        }
    }

    private static Type typeIntersection(Type type1, Type type2) {
        Set<Type> set1 = getTypeSets(type1), set2 = getTypeSets(type2);
        Set<Type> intersection = new HashSet<>();
        for (Type t1 : set1) {
            for (Type t2 : set2) {
                if (t1.isAssignableFrom(t2)) {
                    intersection.add(t2);
                } else if (t2.isAssignableFrom(t1)) {
                    intersection.add(t1);
                }
            }
        }
        return createTypeFromSet(intersection);
    }

    private Type getType(Expression expression) {
        return getTypeFunc.apply(expression);
    }

    private Type typeDiff(Type type1, Type type2) {
        return createTypeFromSet(NncUtils.diffSet(getTypeSets(type1), getTypeSets(type2)));
    }

    private static Type typeUnion(Type type1, Type type2) {
        var set = NncUtils.unionSet(getTypeSets(type1), getTypeSets(type2));
        var toRemove = new HashSet<Type>();
        for (Type t1 : set) {
            for (Type t2 : set) {
                if (t1 != t2 && t2.isAssignableFrom(t1)) {
                    toRemove.add(t1);
                }
            }
        }
        return createTypeFromSet(NncUtils.diffSet(set, toRemove));
    }

    private static Set<Type> getTypeSets(Type type) {
        if (type instanceof UnionType unionType) {
            return unionType.getMembers();
        } else {
            return Set.of(type);
        }
    }

    private static Type createTypeFromSet(Set<Type> set) {
        if (set.isEmpty()) {
            return NeverType.instance;
        }
        if (set.size() == 1) {
            return set.iterator().next();
        } else {
            return new UnionType(set);
        }
    }

}
