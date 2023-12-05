package tech.metavm.common;

import tech.metavm.object.type.rest.dto.TypeDTO;

import java.util.Collection;
import java.util.List;

public record MetaPatch(
        long baseVersion,
        long version,
        List<TypeDTO> changedTypes,
        List<Long> removedTypeIds
) {
}
