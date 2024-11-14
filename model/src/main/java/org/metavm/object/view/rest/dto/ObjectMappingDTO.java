package org.metavm.object.view.rest.dto;

public record ObjectMappingDTO(
        String id,
        String name,
        String sourceType,
        String targetType,
        boolean isDefault,
        boolean builtin,
        ObjectMappingParam param
) implements MappingDTO {
    @Override
    public int getKind() {
        return 1;
    }
}
