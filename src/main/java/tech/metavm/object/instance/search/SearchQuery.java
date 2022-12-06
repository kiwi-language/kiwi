package tech.metavm.object.instance.search;


import tech.metavm.object.instance.query.Expression;

public record SearchQuery (
        long tenantId,
        long typeId,
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
