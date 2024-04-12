package tech.metavm.object.type.rest.dto;

import tech.metavm.flow.rest.FlowDTO;

import java.util.List;

public record BatchSaveRequest(
        List<TypeDTO> types,
        List<FlowDTO> functions,
        List<ParameterizedFlowDTO> parameterizedFlows,
        boolean skipFlowPreprocess
) {
}
