package org.metavm.entity.mocks;

import org.metavm.wire.Wire;

@Wire(201)
public record Product(
        String name,
        double price,
        int stock
) {

}
