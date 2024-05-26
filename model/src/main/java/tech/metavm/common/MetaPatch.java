package tech.metavm.common;

import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.type.rest.dto.TypeDefDTO;
import tech.metavm.object.view.rest.dto.MappingDTO;

import java.util.List;

public record MetaPatch(
        long baseVersion,
        long version,
        List<TypeDefDTO> changedTypeDefs,
        List<String> removedTypeDefIds,
        List<MappingDTO> changedMappings,
        List<String> removedMappingIds,
        List<FlowDTO> changedFunctions,
        List<String> removedFunctionIds
) {
}
