package org.metavm.object.type.rest.dto;

import org.metavm.common.rest.dto.BaseDTO;

import javax.annotation.Nullable;

public record ConstraintDTO(
        String id,
        int kind,
        String typeId,
        String name,
        String message,
        ConstraintParam param
) implements BaseDTO  {

    public <T> T getParam() {
        //noinspection unchecked
        return (T) param;
    }

}
