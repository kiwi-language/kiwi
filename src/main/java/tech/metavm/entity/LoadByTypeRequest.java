package tech.metavm.entity;

import tech.metavm.object.type.Type;

public record LoadByTypeRequest (
    Type type,
    long startId,
    long limit
) {
}
