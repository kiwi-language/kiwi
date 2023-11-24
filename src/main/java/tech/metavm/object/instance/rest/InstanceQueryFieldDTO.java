package tech.metavm.object.instance.rest;

import javax.annotation.Nullable;

public record InstanceQueryFieldDTO(
        long fieldId,
        @Nullable FieldValue value,
        @Nullable FieldValue min,
        @Nullable FieldValue max
) {
}
