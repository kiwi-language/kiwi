package tech.metavm.object.instance.rest;

import java.util.List;

public record InstanceQuery(
        long typeId,
        String searchText,
        int page,
        int pageSize,
        boolean includeSubTypes,
        boolean includeContextTypes,
        List<Long> created,
        List<Long> removed
) {

    public long start() {
        return (long) (page - 1) * pageSize;
    }

    public long limit() {
        return pageSize;
    }

}
