package tech.metavm.object.instance.rest;

import javax.annotation.Nullable;

public record InstanceQueryFieldDTO(
        String fieldId,
        @Nullable FieldValue value,
        @Nullable FieldValue min,
        @Nullable FieldValue max
) {
}
