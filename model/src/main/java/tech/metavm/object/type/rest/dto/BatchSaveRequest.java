package tech.metavm.object.type.rest.dto;

import tech.metavm.flow.rest.FlowDTO;

import java.util.List;

public record BatchSaveRequest(
        List<? extends TypeDefDTO> typeDefs,
        List<FlowDTO> functions,
        boolean skipFlowPreprocess
) {
}
