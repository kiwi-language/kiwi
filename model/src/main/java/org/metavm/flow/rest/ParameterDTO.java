package org.metavm.flow.rest;

import org.metavm.common.BaseDTO;

import javax.annotation.Nullable;
import java.util.Map;

public record ParameterDTO(
        String id,
        String name,
        String code,
        String type,
        @Nullable ValueDTO condition,
        @Nullable String templateId,
        Map<String, String> attributes,
        String callableId
) implements BaseDTO {
    public static ParameterDTO create(String id, String name, String typeId) {
        return new ParameterDTO(id, name, null, typeId, null, null, Map.of(), null);
    }

    public static ParameterDTO create(String id, String name, @Nullable String code, String typeId) {
        return new ParameterDTO(id, name, code, typeId, null, null, Map.of(), null);
    }
}
