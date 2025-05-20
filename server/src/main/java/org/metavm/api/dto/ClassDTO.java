package org.metavm.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ClassDTO(
        String access,
        String tag,
        @JsonProperty("abstract")
        boolean isAbstract,
        String name,
        String qualifiedName,
        List<ClassTypeDTO> superTypes,
        ConstructorDTO constructor,
        List<FieldDTO> fields,
        List<MethodDTO> methods,
        List<ClassDTO> classes,
        List<EnumConstantDTO> enumConstants,
        String beanName,
        String label
) {
}
