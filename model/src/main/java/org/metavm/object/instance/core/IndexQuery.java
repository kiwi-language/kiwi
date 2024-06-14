package org.metavm.object.instance.core;

import org.metavm.entity.IndexSearchOp;
import org.metavm.object.instance.IndexKeyRT;

public record IndexQuery(
    IndexKeyRT key,
    IndexSearchOp op,
    boolean desc,
    long limit
) {
}
