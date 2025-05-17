package org.metavm.api.dto;

import java.util.List;

public record ClassDTO(
        String access,
        String tag,
        boolean isAbstract,
        String name,
        String qualifiedName,
        List<ClassTypeDTO> superTypes,
        ConstructorDTO constructor,
        List<FieldDTO> fields,
        List<MethodDTO> methods,
        List<ClassDTO> classes,
        List<EnumConstantDTO> enumConstants
) {
}
