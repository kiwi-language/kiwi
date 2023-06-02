package tech.metavm.object.meta.rest.dto;

import tech.metavm.object.meta.TypeCategory;

import javax.annotation.Nullable;
import java.util.List;

public record TypeDTO(
        Long id,
        String name,
        @Nullable String code,
        int category,
        boolean ephemeral,
        boolean anonymous,
        @Nullable Long nullableTypeId,
        @Nullable Long arrayTypeId,
        Object param
) {

    public static TypeDTO createClass(String name, List<FieldDTO> fieldDTOs) {
        return createClass(null, name, fieldDTOs);
    }

    public static TypeDTO createClass(Long id, String name, List<FieldDTO> fieldDTOs) {
        return new TypeDTO(
                id, name, null, TypeCategory.CLASS.code(), false, false,
                null, null,
                new ClassParamDTO(
                        null, null, fieldDTOs, List.of(), null, null
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
                id, name, null, TypeCategory.CLASS.code(),
                ephemeral, anonymous, null, null,
                new ClassParamDTO(
                        superTypeId,
                        null,
                        fieldDTOs,
                        constraintDTOs,
                        desc,
                        null
                )
        );
    }
}
