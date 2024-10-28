package org.metavm.object.type.rest.dto;

public record FieldRefDTO(
        String declaringType,
        String rawFieldId
) implements PropertyRefDTO {
    @Override
    public int getKind() {
        return 2;
    }
}
