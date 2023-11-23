package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public record InstanceQuery(
        long typeId,
        String searchText,
        int page,
        int pageSize,
        boolean includeSubTypes,
        boolean includeContextTypes,
        List<Long> newlyCreated
) {

    @JsonIgnore
    public long start() {
        return (long) (page - 1) * pageSize;
    }

    @JsonIgnore
    public long limit() {
        return pageSize;
    }

}
