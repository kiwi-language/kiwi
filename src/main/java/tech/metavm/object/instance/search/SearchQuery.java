package tech.metavm.object.instance.search;


import tech.metavm.object.instance.query.Expression;

public record SearchQuery (
        long tenantId,
        long typeId,
        Expression condition,
        int page,
        int pageSize
) {

    int from() {
        return (page - 1) * pageSize;
    }

    int size() {
        return pageSize;
    }

}
