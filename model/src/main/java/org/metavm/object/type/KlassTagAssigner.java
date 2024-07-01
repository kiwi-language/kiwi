package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;

import java.util.Objects;

@EntityType
public class KlassTagAssigner extends Entity {

    public static final IndexDef<KlassTagAssigner> IDX_ALL_FLAGS = IndexDef.create(KlassTagAssigner.class, "allFlags");

    public static KlassTagAssigner getInstance(IEntityContext context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, true),
                "ClassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private final boolean allFlags = true;

    private long start;
    private long next;
    private long max;

    public KlassTagAssigner(long start, long max) {
        this.start = next = start;
        this.max = max;
    }

    public static void initialize(IEntityContext context, GlobalKlassTagAssigner globalKlassTagAssigner) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, true);
        if(existing != null)
            throw new IllegalStateException("ClassTagAssigner already exists");
        var range = globalKlassTagAssigner.allocate(1000000);
        context.bind(new KlassTagAssigner(range[0], range[1]));
    }

    public long next() {
        if(next >= max)
            throw new IllegalStateException("No more class tags available");
        return next++;
    }

    public void assign(long start, long max) {
        this.start = next = start;
        this.max = max;
    }

    public long getStart() {
        return start;
    }

    public long getNext() {
        return next;
    }

    public long getMax() {
        return max;
    }
}
