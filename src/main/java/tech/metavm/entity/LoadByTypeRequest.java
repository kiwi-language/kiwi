package tech.metavm.entity;

import tech.metavm.object.meta.Type;

public record LoadByTypeRequest (
    Type type,
    long startId,
    long limit
) {
}
