package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(40)
@Entity
public class GlobalKlassTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<GlobalKlassTagAssigner> IDX_ALL_FLAGS = IndexDef.create(GlobalKlassTagAssigner.class,
            1, e -> List.of(Instances.booleanInstance(e.allFlags)));

    public static GlobalKlassTagAssigner initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if (existing != null)
            throw new IllegalStateException("GlobalKlassTagAssigner already exists");
        return context.bind(new GlobalKlassTagAssigner(context.allocateRootId()));
    }

    public static GlobalKlassTagAssigner getInstance(IInstanceContext context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "GlobalKlassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private final boolean allFlags = true;
    private long next = 1000000;

    public GlobalKlassTagAssigner(Id id) {
        super(id);
    }

    public long[] allocate(long size) {
        return new long[]{next, next += size};
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
