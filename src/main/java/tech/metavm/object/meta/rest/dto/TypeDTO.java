package tech.metavm.object.meta.rest.dto;

import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.NncUtils;

import java.util.List;

public record TypeDTO(
        Long id,
        String name,
        Long superTypeId,
        int category,
        boolean ephemeral,
        boolean anonymous,
        TypeDTO rawType,
        Long rawTypeId,
        List<TypeDTO> typeArguments,
        List<Long> typeArgumentIds,
        List<TypeDTO> typeMembers,
        List<Long> typeMemberIds,
        String desc,
        List<FieldDTO> fields,
        List<ConstraintDTO> constraints,
        List<EnumConstantDTO> enumConstants,
        Long nullableTypeId,
        Long arrayTypeId
) {
    public static TypeDTO create(
            Long id,
            String name,
            Long superTypeId,
            int category,
            boolean ephemeral,
            boolean anonymous,
            TypeDTO rawType,
            List<TypeDTO> typeArguments,
            List<TypeDTO> typeMembers,
            String desc,
            List<FieldDTO> fields,
            List<ConstraintDTO> constraints,
            List<EnumConstantDTO> enumConstants
    ) {
        return new TypeDTO(
                id,
                name,
                superTypeId,
                category,
                ephemeral,
                anonymous,
                rawType,
                NncUtils.get(rawType, TypeDTO::id),
                typeArguments,
                NncUtils.map(typeArguments, TypeDTO::id),
                typeMembers,
                NncUtils.map(typeMembers, TypeDTO::id),
                desc,
                fields,
                constraints,
                enumConstants,
                null,
                null
        );
    }

    public static TypeDTO createClass(
            Long id,
            String name,
            long superTypeId,
            boolean ephemeral,
            boolean anonymous,
            TypeDTO rawType,
            List<TypeDTO> typeArguments,
            List<TypeDTO> typeMembers,
            String desc,
            List<FieldDTO> fields,
            List<ConstraintDTO> constraints
    ) {
        return new TypeDTO(
                id,
                name,
                superTypeId,
                TypeCategory.CLASS.code(),
                ephemeral,
                anonymous,
                rawType,
                NncUtils.get(rawType, TypeDTO::id),
                typeArguments,
                NncUtils.map(typeArguments, TypeDTO::id),
                typeMembers,
                NncUtils.map(typeMembers, TypeDTO::id),
                desc,
                fields,
                constraints,
                List.of(),
                null,
                null
        );
    }

    public static TypeDTO createPrimitive(
            Long id,
            String name,
            long superTypeId,
            boolean ephemeral,
            boolean anonymous,
            String desc
    ) {
        return new TypeDTO(
                id,
                name,
                superTypeId,
                TypeCategory.PRIMITIVE.code(),
                ephemeral,
                anonymous,
                null,
                null,
                null,
                null,
                null,
                null,
                desc,
                List.of(),
                List.of(),
                List.of(),
                null,
                null
        );
    }

    public static TypeDTO createEnum(
            Long id,
            String name,
            long superTypeId,
            boolean ephemeral,
            boolean anonymous,
            String desc,
            List<FieldDTO> fields,
            List<ConstraintDTO> constraints,
            List<EnumConstantDTO> enumConstants
    ) {
        return new TypeDTO(
                id,
                name,
                superTypeId,
                TypeCategory.ENUM.code(),
                ephemeral,
                anonymous,
                null,
                null,
                null,
                null,
                null,
                null,
                desc,
                fields,
                constraints,
                enumConstants,
                null,
                null
        );
    }

}
