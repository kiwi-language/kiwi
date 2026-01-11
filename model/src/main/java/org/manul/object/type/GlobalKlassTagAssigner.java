package org.manul.object.type;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.IInstanceContext;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

@Wire(40)
@Entity
public class GlobalKlassTagAssigner extends org.manul.entity.Entity {

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
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
