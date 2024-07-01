package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;

import java.util.Objects;

@EntityType
public class GlobalKlassTagAssigner extends Entity {

    public static final IndexDef<GlobalKlassTagAssigner> IDX_ALL_FLAGS = IndexDef.create(GlobalKlassTagAssigner.class, "allFlags");

    public static GlobalKlassTagAssigner initialize(IEntityContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, true);
        if (existing != null)
            throw new IllegalStateException("GlobalKlassTagAssigner already exists");
        return context.bind(new GlobalKlassTagAssigner());
    }

    public static GlobalKlassTagAssigner getInstance(IEntityContext context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, true),
                "GlobalKlassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private final boolean allFlags = true;
    private long next = 1000000;

    public long[] allocate(long size) {
        return new long[]{next, next += size};
    }

}
