package org.metavm.object.type.rest.dto;

public record CapturedTypeVariableDTO(String id, String uncertainType, String scopeId, int index) implements TypeDefDTO {
    @Override
    public int getDefKind() {
        return 3;
    }
}
