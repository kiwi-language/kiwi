package org.metavm.api.dto;

import java.util.List;

public record ConstructorDTO(
        List<ParameterDTO> parameters
) {
}
