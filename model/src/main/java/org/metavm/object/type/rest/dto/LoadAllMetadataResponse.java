package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.view.rest.dto.MappingDTO;

import java.util.List;

public record LoadAllMetadataResponse(
    long metaVersion,
    List<TypeDefDTO> typeDefs,
    List<MappingDTO> mappings,
    List<FlowDTO> functions
) {

}
