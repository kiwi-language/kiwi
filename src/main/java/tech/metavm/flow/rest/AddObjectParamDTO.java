package tech.metavm.flow.rest;

import java.util.List;

public record AddObjectParamDTO(
        Long typeId,
        List<FieldParamDTO> fieldParams
) {


}
