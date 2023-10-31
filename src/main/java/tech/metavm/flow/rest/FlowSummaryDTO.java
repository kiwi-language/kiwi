package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record FlowSummaryDTO(
    Long id,
    String name,
    Long typeId,
    List<ParameterDTO> parameters,
    RefDTO returnTypeRef,
    boolean inputRequired,
    boolean isConstructor
) {

}
