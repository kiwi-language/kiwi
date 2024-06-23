package org.metavm.object.type.rest.dto;

import org.metavm.flow.rest.FlowDTO;

import java.util.List;

public record BatchSaveRequest(
        List<? extends TypeDefDTO> typeDefs,
        List<FlowDTO> functions,
        boolean skipFlowPreprocess
) {
}
