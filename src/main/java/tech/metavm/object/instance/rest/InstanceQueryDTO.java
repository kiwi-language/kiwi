package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.entity.InstanceQueryField;

import javax.annotation.Nullable;
import java.util.List;

public record InstanceQueryDTO(
        long typeId,
        String searchText,
        @Nullable String expression,
        List<InstanceQueryFieldDTO> fields,
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
