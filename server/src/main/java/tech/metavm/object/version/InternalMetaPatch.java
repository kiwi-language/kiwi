package tech.metavm.object.version;

import java.util.List;

public record InternalMetaPatch(
        long baseVersion,
        long version,
        List<String> changedTypeIds,
        List<String> removedTypeIds,
        List<String> changedMappingIds,
        List<String> removedMappingIds,
        List<String> changedFunctionIds,
        List<String> removedFunctionIds
) {
}
