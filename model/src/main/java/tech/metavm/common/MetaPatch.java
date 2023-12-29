package tech.metavm.common;

import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.view.rest.dto.MappingDTO;

import java.util.List;

public record MetaPatch(
        long baseVersion,
        long version,
        List<TypeDTO> changedTypes,
        List<Long> removedTypeIds,
        List<MappingDTO> changedMappings,
        List<Long> removedMappingIds,
        List<FlowDTO> changedFunctions,
        List<Long> removedFunctionIds
) {
}
