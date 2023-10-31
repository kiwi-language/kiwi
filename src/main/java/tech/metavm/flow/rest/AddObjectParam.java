package tech.metavm.flow.rest;

import tech.metavm.dto.RefDTO;

import javax.annotation.Nullable;
import java.util.List;

public record AddObjectParam(
        RefDTO typeRef,
        List<FieldParamDTO> fieldParams,
        @Nullable MasterRefDTO master
) {


}
