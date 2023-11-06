package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.IndexField;

public record InstanceIndexQueryItem(
        IndexField indexItem,
        Instance value
) {
}
