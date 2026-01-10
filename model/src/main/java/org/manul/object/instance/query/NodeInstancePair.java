package org.manul.object.instance.query;

import org.manul.object.instance.core.Value;

public record NodeInstancePair(
        InstanceNode<?> node,
        Value instance
) {
}
