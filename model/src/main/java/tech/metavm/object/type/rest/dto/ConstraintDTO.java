package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;

import javax.annotation.Nullable;

public record ConstraintDTO(
        String id,
        int kind,
        String typeId,
        String name,
        @Nullable String code,
        String message,
        ConstraintParam param
) implements BaseDTO  {

    public <T> T getParam() {
        //noinspection unchecked
        return (T) param;
    }

}
