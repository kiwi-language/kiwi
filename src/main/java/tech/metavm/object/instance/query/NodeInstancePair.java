package tech.metavm.object.instance.query;

import tech.metavm.object.instance.Instance;

public record NodeInstancePair(
        InstanceNode<?> node,
        Instance instance
) {
}
