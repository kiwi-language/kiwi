package tech.metavm.object.instance.search;


import tech.metavm.expression.Expression;
import tech.metavm.expression.InstanceEvaluationContext;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.ClassInstance;

import java.util.Set;

public record SearchQuery(
        long appId,
        Set<String> types,
        Expression condition,
        boolean includeBuiltin,
        int page,
        int pageSize,
        int extra
) {

    public SearchQuery {
        if(types.isEmpty())
            throw new IllegalArgumentException("types is empty");
    }

    public int from() {
        return (page - 1) * pageSize;
    }

    public int size() {
        return pageSize + extra;
    }

    public int end() {
        return from() + size();
    }

    public boolean match(ClassInstance instance) {
        return types.contains(instance.getType().toExpression()) &&
                (condition == null || ((BooleanInstance) condition.evaluate(new InstanceEvaluationContext(instance))).isTrue());
    }

}
