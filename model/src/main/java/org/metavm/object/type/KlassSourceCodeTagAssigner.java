package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.entity.IndexDef;

import java.util.Objects;

@Entity
public class KlassSourceCodeTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<KlassSourceCodeTagAssigner> IDX_ALL_FLAGS = IndexDef.create(KlassSourceCodeTagAssigner.class, "allFlags");

    public static KlassSourceCodeTagAssigner getInstance(IEntityContext context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, true),
                "ClassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private final boolean allFlags = true;

    private int next = 1000000;

    public KlassSourceCodeTagAssigner() {
    }

    public static void initialize(IEntityContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, true);
        if(existing != null)
            throw new IllegalStateException("ClassTagAssigner already exists");
        context.bind(new KlassSourceCodeTagAssigner());
    }

    public int next() {
        return next++;
    }

    public int getNext() {
        return next;
    }
}
