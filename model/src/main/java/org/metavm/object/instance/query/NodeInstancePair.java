package org.metavm.object.instance.query;

import org.metavm.object.instance.core.Instance;

public record NodeInstancePair(
        InstanceNode<?> node,
        Instance instance
) {
}
