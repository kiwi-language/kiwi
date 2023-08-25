package tech.metavm.flow.rest;

import java.util.List;

public record InputParamDTO(
        Long typeId,
        List<InputFieldDTO> fields
) {
}
