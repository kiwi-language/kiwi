package org.metavm.object.type;

import org.metavm.entity.Entity;

public record IdRange(
        long start,
        long end,
        Class<? extends Entity> entityType
) {

    public static IdRange valueOf(long start, long end, Class<? extends Entity> entityType) {
        return new IdRange(start, end, entityType);
    }

    public boolean contains(long id) {
        return id >= start && id < end;
    }

}
