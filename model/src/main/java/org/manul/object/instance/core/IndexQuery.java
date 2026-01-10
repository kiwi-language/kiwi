package org.manul.object.instance.core;

import org.manul.entity.IndexSearchOp;
import org.manul.object.instance.IndexKeyRT;

public record IndexQuery(
    IndexKeyRT key,
    IndexSearchOp op,
    boolean desc,
    long limit
) {
}
