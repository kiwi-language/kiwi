package tech.metavm.object.version;

import java.util.List;

public record InternalMetaPatch(
        long baseVersion,
        long version,
        List<String> changedTypeDefIds,
        List<String> removedTypeDefIds,
        List<String> changedMappingIds,
        List<String> removedMappingIds,
        List<String> changedFunctionIds,
        List<String> removedFunctionIds
) {
}
