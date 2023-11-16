package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;

public record InstanceQueryField(
        Field field,
        Instance value
) {
}
