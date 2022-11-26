package tech.metavm.infra;

import tech.metavm.object.meta.TypeCategory;

public record RegionInfo(
        TypeCategory typeCategory,
        long start,
        long end
) {

    public Boolean contains(long id) {
        return id >= start && id < end;
    }
}
