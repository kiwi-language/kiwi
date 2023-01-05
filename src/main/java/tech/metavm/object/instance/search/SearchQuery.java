package tech.metavm.object.instance.search;


import tech.metavm.object.instance.query.Expression;

import java.util.Set;

public record SearchQuery (
        long tenantId,
        Set<Long> typeIds,
        Expression condition,
        int page,
        int pageSize
) {

    public int from() {
        return (page - 1) * pageSize;
    }

    public int size() {
        return pageSize;
    }

    public int end() {
        return page * pageSize;
    }

}
