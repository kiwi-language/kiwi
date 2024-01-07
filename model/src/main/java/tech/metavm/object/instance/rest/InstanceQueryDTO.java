package tech.metavm.object.instance.rest;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.annotation.Nullable;
import java.util.List;

public record InstanceQueryDTO(
        long typeId,
        @Nullable Long sourceMappingId,
        String searchText,
        @Nullable String expression,
        List<InstanceQueryFieldDTO> fields,
        int page,
        int pageSize,
        boolean includeSubTypes,
        boolean includeContextTypes,
        List<String> createdIds
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
