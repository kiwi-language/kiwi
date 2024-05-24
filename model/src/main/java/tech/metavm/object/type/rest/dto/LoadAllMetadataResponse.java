package tech.metavm.object.type.rest.dto;

import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.view.rest.dto.MappingDTO;

import java.util.List;

public record LoadAllMetadataResponse(
    long metaVersion,
    List<TypeDefDTO> types,
    List<MappingDTO> mappings,
    List<FlowDTO> functions
) {

}
