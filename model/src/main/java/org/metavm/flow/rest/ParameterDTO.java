package org.metavm.flow.rest;

import org.metavm.common.rest.dto.BaseDTO;

import javax.annotation.Nullable;
import java.util.Map;

public record ParameterDTO(
        String id,
        String name,
        String type,
        @Nullable ValueDTO condition,
        @Nullable String templateId,
        Map<String, String> attributes,
        String callableId
) implements BaseDTO {

    public static ParameterDTO create(String id, String name, String typeId) {
        return new ParameterDTO(id, name, typeId, null, null, Map.of(), null);
    }

}
