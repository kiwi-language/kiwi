package tech.metavm.object.meta.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import tech.metavm.dto.BaseDTO;
import tech.metavm.dto.RefDTO;
import tech.metavm.object.meta.ClassSource;
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
        @Nullable RefDTO nullableTypeRef,
        @Nullable RefDTO arrayTypeRef,
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
        @JsonTypeIdResolver(TypeParamTypeIdResolver.class)
        Object param
) implements BaseDTO {

    public static TypeDTO createClass(String name, List<FieldDTO> fieldDTOs) {
        return createClass(null, name, fieldDTOs);
    }

    public static TypeDTO createClass(Long id, String name, List<FieldDTO> fieldDTOs) {
        return new TypeDTO(
                id, null, name, null, TypeCategory.CLASS.code(),
                false, false,
                null, null,
                new ClassParamDTO(
                        null, null,
                        List.of(),
                        List.of(),
                        ClassSource.RUNTIME.code(),
                        fieldDTOs, List.of(), List.of(), List.of(), null, null, null,
                        List.of(), List.of(), null, List.of(), List.of()
                )
        );
    }

    @JsonIgnore
    public ClassParamDTO getClassParam() {
        return (ClassParamDTO) param;
    }

    public static TypeDTO createClass(Long id,
                                      Long tmpId,
                                      String name,
                                      Long superTypeId,
                                      boolean anonymous,
                                      boolean ephemeral,
                                      List<FieldDTO> fieldDTOs,
                                      List<ConstraintDTO> constraintDTOs,
                                      String desc) {
        return new TypeDTO(
                id, tmpId, name, null, TypeCategory.CLASS.code(),
                ephemeral, anonymous, null, null,
                new ClassParamDTO(
                        RefDTO.ofId(superTypeId),
                        null,
                        List.of(),
                        List.of(),
                        ClassSource.RUNTIME.code(),
                        fieldDTOs,
                        List.of(),
                        constraintDTOs,
                        List.of(),
                        null,
                        desc,
                        null,
                        List.of(),
                        List.of(),
                        null,
                        List.of(),
                        List.of()
                )
        );
    }

    @JsonIgnore
    public TypeVariableParamDTO getTypeVariableParam() {
        return (TypeVariableParamDTO) param;
    }

    @JsonIgnore
    public ArrayTypeParamDTO getArrayTypeParam() {
        return (ArrayTypeParamDTO) param;
    }

    @JsonIgnore
    public UnionTypeParamDTO getUnionParam() {
        return (UnionTypeParamDTO) param;
    }
}
