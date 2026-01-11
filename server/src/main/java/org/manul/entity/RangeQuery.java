package org.manul.entity;

public record RangeQuery(
        long startId,
        long limit
) {

    RangeQuery extend() {
        return new RangeQuery(startId, limit + 1);
    }

}
