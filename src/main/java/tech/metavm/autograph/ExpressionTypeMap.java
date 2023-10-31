package tech.metavm.autograph;

import tech.metavm.expression.Expression;
import tech.metavm.object.meta.Type;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ExpressionTypeMap {

    public static final ExpressionTypeMap EMPTY = new ExpressionTypeMap(Map.of());

    private final Map<Expression, Type> map;

    public ExpressionTypeMap(Map<Expression, Type> map) {
        this.map = new HashMap<>(map);
    }

    public Type getType(Expression expression) {
        return map.getOrDefault(expression, expression.getType());
    }

    public ExpressionTypeMap union(ExpressionTypeMap that) {
        return new ExpressionTypeMap(TypeNarrower.unionResults(map, that.map));
    }

    public ExpressionTypeMap merge(ExpressionTypeMap that) {
        return new ExpressionTypeMap(TypeNarrower.mergeResults(map, that.map));
    }

    public Map<Expression, Type> toMap() {
        return Collections.unmodifiableMap(map);
    }

}
