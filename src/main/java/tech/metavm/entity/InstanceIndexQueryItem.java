package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.IndexField;

public record InstanceIndexQueryItem(
        IndexField indexItem,
        Instance value
) {
}
