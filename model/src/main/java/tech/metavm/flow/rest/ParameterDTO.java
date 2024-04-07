package tech.metavm.flow.rest;

import tech.metavm.common.BaseDTO;

import javax.annotation.Nullable;
import java.util.List;

public record ParameterDTO(
        String id,
        String name,
        String code,
        String typeId,
        @Nullable ValueDTO condition,
        @Nullable String templateId,
        String callableId,
        List<String> capturedTypeIds
) implements BaseDTO {
    public static ParameterDTO create(String id, String name, String typeId) {
        return new ParameterDTO(id, name, null, typeId, null, null, null, List.of());
    }
}
