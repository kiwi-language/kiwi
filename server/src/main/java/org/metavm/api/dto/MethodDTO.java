package org.metavm.api.dto;

import java.util.List;

public record MethodDTO(
        String access,
        boolean isAbstract,
        String name,
        List<ParameterDTO> parameters,
        TypeDTO returnType
) {
}
