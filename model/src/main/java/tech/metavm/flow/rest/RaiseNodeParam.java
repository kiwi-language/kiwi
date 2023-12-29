package tech.metavm.flow.rest;

import javax.annotation.Nullable;

public record RaiseNodeParam(
        Integer parameterKind,
        @Nullable ValueDTO message,
        @Nullable ValueDTO exception
) {

}
