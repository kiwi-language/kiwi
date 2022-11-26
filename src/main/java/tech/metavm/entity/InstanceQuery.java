package tech.metavm.entity;

import java.util.List;

public record InstanceQuery(
        long typeId,
        String searchText,
        int page,
        int pageSize,
        List<InstanceQueryField> fields
) {
}
