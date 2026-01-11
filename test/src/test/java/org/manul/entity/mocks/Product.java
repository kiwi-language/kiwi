package org.manul.entity.mocks;

import org.manul.wire.Wire;

@Wire(201)
public record Product(
        String name,
        double price,
        int stock
) {

}
