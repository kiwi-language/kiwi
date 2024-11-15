package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.FlowDTO;

import java.util.List;

public record LoadAllMetadataResponse(
    long metaVersion,
    List<TypeDefDTO> typeDefs,
    List<FlowDTO> functions
) {

}
