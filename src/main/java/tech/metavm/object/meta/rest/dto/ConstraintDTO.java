package tech.metavm.object.meta.rest.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.object.meta.rest.ConstraintParamTypeIdResolver;

public record ConstraintDTO(
        Long id,
        int kind,
        long typeId,
        String message,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "kind")
        @JsonTypeIdResolver(ConstraintParamTypeIdResolver.class)
        Object param
) {

    public <T> T getParam() {
        return (T) param;
    }

}
