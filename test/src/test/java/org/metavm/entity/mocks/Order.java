package org.metavm.entity.mocks;

import org.metavm.wire.Wire;

@Wire
public record Order(
        long productId,
        int quantity
) {
}
