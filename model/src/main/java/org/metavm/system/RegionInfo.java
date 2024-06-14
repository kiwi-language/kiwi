package org.metavm.system;

import org.metavm.object.type.TypeCategory;

public record RegionInfo(
        TypeCategory typeCategory,
        long start,
        long end
) {

    public Boolean contains(long id) {
        return id >= start && id < end;
    }
}
