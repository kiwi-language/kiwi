package tech.metavm.flow.rest;

import java.util.List;

public record AddObjectParamDTO(
        long typeId,
        List<FieldParamDTO> fieldParams
) {


}
