package org.metavm.object.instance.query;

import org.metavm.object.instance.core.Value;

public record NodeInstancePair(
        InstanceNode<?> node,
        Value instance
) {
}
