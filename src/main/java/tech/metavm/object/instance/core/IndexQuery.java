package tech.metavm.object.instance.core;

import tech.metavm.entity.IndexSearchOp;
import tech.metavm.object.instance.IndexKeyRT;

public record IndexQuery(
    IndexKeyRT key,
    IndexSearchOp op,
    boolean desc,
    long limit
) {
}
