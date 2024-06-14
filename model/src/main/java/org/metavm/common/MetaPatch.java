package org.metavm.common;

import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.type.rest.dto.TypeDefDTO;
import org.metavm.object.view.rest.dto.MappingDTO;

import java.util.List;

public record MetaPatch(
        long baseVersion,
        long version,
        boolean reset,
        List<TypeDefDTO> changedTypeDefs,
        List<String> removedTypeDefIds,
        List<MappingDTO> changedMappings,
        List<String> removedMappingIds,
        List<FlowDTO> changedFunctions,
        List<String> removedFunctionIds
) {
}
