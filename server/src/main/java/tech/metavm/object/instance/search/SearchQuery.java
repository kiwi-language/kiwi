package tech.metavm.object.instance.search;


import tech.metavm.expression.Expression;

import java.util.Set;

public record SearchQuery (
        long appId,
        Set<Long> typeIds,
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
