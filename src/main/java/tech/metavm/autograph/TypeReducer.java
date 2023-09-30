package tech.metavm.autograph;

import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.expression.*;
import tech.metavm.object.meta.Type;
import tech.metavm.object.meta.UnionType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Null;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TypeReducer {

    private final Function<Expression,Type> getTypeFunc;

    public TypeReducer(Function<Expression, Type> getTypeFunc) {
        this.getTypeFunc = getTypeFunc;
    }

    public Map<Expression, Type> reduceType(Expression expression) {
        return process(expression, false);
    }

    private Map<Expression, Type> process(Expression expression, boolean negated) {
        if (expression instanceof InstanceOfExpression instanceOfExpr) {
            return processInstanceOf(instanceOfExpr, negated);
        } else if (expression instanceof BinaryExpression binaryExpression) {
            return processBinary(binaryExpression, negated);
        } else if (expression instanceof UnaryExpression unaryExpression) {
            return processUnary(unaryExpression, negated);
        }
        return Map.of();
    }

    private Map<Expression, Type> processInstanceOf(InstanceOfExpression expression, boolean negated) {
        var type = negated ? typeDiff(getType(expression), expression.getTargetType()) :
                typeIntersection(getType(expression), expression.getTargetType());
        return type != null ? Map.of(expression.getOperand(), type) : Map.of();
    }

    private Map<Expression, Type> processBinary(BinaryExpression binaryExpression, boolean negated) {
        var op = binaryExpression.getOperator();
        var first = binaryExpression.getFirst();
        var second = binaryExpression.getSecond();
        final Map<Expression, Type> result = new HashMap<>();
        if (op == Operator.AND || op == Operator.OR) {
            if (negated) {
                op = op == Operator.AND ? Operator.OR : Operator.AND;
            }
            var firstResult = process(first, negated);
            var secondResult = process(second, negated);
            if (op == Operator.AND) {
                result.putAll(mergeResults(firstResult, secondResult));
            } else {
                firstResult.forEach((expr, type) -> {
                    var secondType = secondResult.get(expr);
                    if (secondType != null) {
                        result.put(expr, typeUnion(type, secondType));
                    }
                });
            }
        } else if (op == Operator.EQ || op == Operator.NE) {
            if (negated) {
                op = op == Operator.NE ? Operator.EQ : Operator.NE;
            }
            if (op == Operator.EQ) {
                var intersection = typeIntersection(getType(first), getType(second));
                if (intersection != null) {
                    if (ExpressionUtil.isNotConstant(first)) {
                        result.put(first, intersection);
                    }
                    if (ExpressionUtil.isNotConstant(second)) {
                        result.put(second, intersection);
                    }
                }
            } else {
                if (ExpressionUtil.isNotConstant(first)) {
                    result.put(first, typeDiff(getType(first), getType(second)));
                }
                if (ExpressionUtil.isNotConstant(second)) {
                    result.put(second, typeDiff(getType(second), getType(first)));
                }
            }
        }
        return result;
    }

    public Map<Expression, Type> mergeResults(Map<Expression, Type> firstResult, Map<Expression, Type> secondResult) {
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

    private Map<Expression, Type> processUnary(UnaryExpression unaryExpression, boolean negated) {
        var operand = unaryExpression.getOperand();
        if (getType(operand).isNotNull()) {
            return Map.of();
        }
        var op = unaryExpression.getOperator();
        if (op == Operator.IS_NOT_NULL || op == Operator.IS_NULL) {
            if (negated) {
                op = op == Operator.IS_NULL ? Operator.IS_NOT_NULL : Operator.IS_NULL;
            }
            if (op == Operator.IS_NOT_NULL) {
                return Map.of(operand, getType(operand).getUnderlyingType());
            } else {
                return Map.of(operand, ModelDefRegistry.getType(Null.class));
            }
        } else if (op == Operator.NOT) {
            return process(operand, !negated);
        } else {
            return Map.of();
        }
    }

    private Type typeIntersection(Type type1, Type type2) {
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

    private Type typeUnion(Type type1, Type type2) {
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

    private Set<Type> getTypeSets(Type type) {
        if (type instanceof UnionType unionType) {
            return unionType.getMembers();
        } else {
            return Set.of(type);
        }
    }

    private Type createTypeFromSet(Set<Type> set) {
        if (set.isEmpty()) {
            return null;
        }
        if (set.size() == 1) {
            return set.iterator().next();
        } else {
            return new UnionType(null, set);
        }
    }

}
