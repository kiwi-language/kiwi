package org.metavm.object.version.rest.dto;

import java.util.List;

public record VersionDTO(
        long version,
        List<String> changedTypeIds,
        List<String> removedTypeIds
) {
}
