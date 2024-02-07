package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;

import javax.annotation.Nullable;

public record ConstraintDTO(
        Long id,
        Long tmpId,
        int kind,
        RefDTO typeRef,
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
