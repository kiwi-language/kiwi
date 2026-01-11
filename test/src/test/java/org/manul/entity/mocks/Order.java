package org.manul.entity.mocks;

import org.manul.wire.Wire;

@Wire
public record Order(
        long productId,
        int quantity
) {
}
