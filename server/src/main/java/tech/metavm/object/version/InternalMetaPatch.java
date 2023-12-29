package tech.metavm.object.version;

import java.util.List;

public record InternalMetaPatch(
        long baseVersion,
        long version,
        List<Long> changedTypeIds,
        List<Long> removedTypeIds,
        List<Long> changedMappingIds,
        List<Long> removedMappingIds,
        List<Long> changedFunctionIds,
        List<Long> removedFunctionIds
) {
}
