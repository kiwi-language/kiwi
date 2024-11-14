package org.metavm.flow.rest;

import org.metavm.common.rest.dto.BaseDTO;

import java.util.List;

public record LambdaDTO(
    String id,
    List<ParameterDTO> parameters,
    String returnType,
    CodeDTO scope
) implements BaseDTO {
}
