package tech.metavm.flow.rest;

import java.util.List;

public record UpdateObjectParamDTO(
        ValueDTO objectId,
        List<FieldParamDTO> fields
) {
}
