package tech.metavm.object.meta.rest.dto;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.flow.rest.NodeParamTypeIdResolver;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.rest.TypeParamTypeIdResolver;

import javax.annotation.Nullable;
import java.util.List;

public record TypeDTO(
        Long id,
        Long tmpId,
        String name,
        @Nullable String code,
        int category,
        boolean ephemeral,
        boolean anonymous,
        @Nullable Long nullableTypeId,
        @Nullable Long arrayTypeId,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
        @JsonTypeIdResolver(TypeParamTypeIdResolver.class)
        Object param
) {

    public static TypeDTO createClass(String name, List<FieldDTO> fieldDTOs) {
        return createClass(null, name, fieldDTOs);
    }

    public static TypeDTO createClass(Long id, String name, List<FieldDTO> fieldDTOs) {
        return new TypeDTO(
                id, null, name, null, TypeCategory.CLASS.code(), false, false,
                null, null,
                new ClassParamDTO(
                        null, null, fieldDTOs, List.of(), List.of(), null, null
                )
        );
    }

    public static TypeDTO createClass(Long id,
                                      String name,
                                      Long superTypeId,
                                      boolean anonymous,
                                      boolean ephemeral,
                                      List<FieldDTO> fieldDTOs,
                                      List<ConstraintDTO> constraintDTOs,
                                      String desc) {
        return new TypeDTO(
                id, null, name, null, TypeCategory.CLASS.code(),
                ephemeral, anonymous, null, null,
                new ClassParamDTO(
                        superTypeId,
                        null,
                        fieldDTOs,
                        constraintDTOs,
                        List.of(),
                        desc,
                        null
                )
        );
    }
}
