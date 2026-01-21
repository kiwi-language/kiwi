package org.manul.context;

public record Product(
        long id,
        String name,
        double price,
        long stock,
        boolean available
) {
}
