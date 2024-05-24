package tech.metavm.flow.rest;

import java.util.List;

public record FlowSummaryDTO(
    String id,
    String name,
    String typeId,
    List<ParameterDTO> parameters,
    String returnType,
    boolean inputRequired,
    boolean isConstructor,
    int state
) {

}
