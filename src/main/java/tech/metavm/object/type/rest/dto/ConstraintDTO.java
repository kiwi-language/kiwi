package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.common.BaseDTO;
import tech.metavm.object.type.rest.ConstraintParamTypeIdResolver;

public record ConstraintDTO(
        Long tmpId,
        Long id,
        int kind,
        long typeId,
        String message,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind", include = JsonTypeInfo.As.EXISTING_PROPERTY)
        @JsonTypeIdResolver(ConstraintParamTypeIdResolver.class)
        Object param
) implements BaseDTO  {

    public <T> T getParam() {
        //noinspection unchecked
        return (T) param;
    }

}
