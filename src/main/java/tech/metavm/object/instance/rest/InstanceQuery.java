package tech.metavm.object.instance.rest;

public record InstanceQuery(
        long typeId,
        String searchText,
        int page,
        int pageSize,
        boolean includeSubTypes,
        boolean includeContextTypes
) {

    public long start() {
        return (long) (page - 1) * pageSize;
    }

    public long limit() {
        return pageSize;
    }

}
