package org.metavm.api.dto;

import org.jsonk.Json;
import org.jsonk.JsonProperty;

import java.util.List;

@Json
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
