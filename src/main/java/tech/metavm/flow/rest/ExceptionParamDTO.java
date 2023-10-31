package tech.metavm.flow.rest;

import javax.annotation.Nullable;

public record ExceptionParamDTO(
        Integer parameterKind,
        @Nullable ValueDTO message,
        @Nullable ValueDTO exception
) {

}
