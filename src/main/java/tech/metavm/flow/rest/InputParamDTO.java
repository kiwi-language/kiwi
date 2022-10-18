package tech.metavm.flow.rest;

import java.util.List;

public record InputParamDTO(
        long typeId,
        List<InputFieldDTO> fields
) {
}
