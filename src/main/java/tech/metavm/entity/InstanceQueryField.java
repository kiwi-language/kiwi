package tech.metavm.entity;

import tech.metavm.object.meta.Field;

public record InstanceQueryField(
        Field field,
        Object value
) {
}
