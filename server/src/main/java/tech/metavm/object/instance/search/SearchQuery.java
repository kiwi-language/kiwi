package tech.metavm.object.instance.search;


import tech.metavm.expression.Expression;
import tech.metavm.object.instance.core.Id;

import java.util.Set;

public record SearchQuery (
        long appId,
        Set<Id> typeIds,
        Expression condition,
        boolean includeBuiltin,
        int page,
        int pageSize,
        int extra
) {

    public int from() {
        return (page - 1) * pageSize;
    }

    public int size() {
        return pageSize + extra;
    }

    public int end() {
        return from() + size();
    }

}
