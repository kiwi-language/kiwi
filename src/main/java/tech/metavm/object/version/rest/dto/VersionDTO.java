package tech.metavm.object.version.rest.dto;

import java.util.List;

public record VersionDTO(
        long version,
        List<Long> changedTypeIds,
        List<Long> removedTypeIds
) {
}
