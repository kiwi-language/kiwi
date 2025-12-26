package org.metavm.object.type;

import lombok.Getter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.EntityRepository;
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

@Wire(64)
@Entity
public class KlassTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<KlassTagAssigner> IDX_ALL_FLAGS = IndexDef.create(KlassTagAssigner.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlags)));

    public static KlassTagAssigner getInstance(EntityRepository context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "ClassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private final boolean allFlags = true;

    @Getter
    private long start;
    private long next;
    @Getter
    private long max;

    public KlassTagAssigner(Id id, long start, long max) {
        super(id);
        this.start = next = start;
        this.max = max;
    }

    public static void initialize(IInstanceContext context, GlobalKlassTagAssigner globalKlassTagAssigner) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if(existing != null)
            throw new IllegalStateException("ClassTagAssigner already exists");
        var range = globalKlassTagAssigner.allocate(1000000);
        context.bind(new KlassTagAssigner(context.allocateRootId(), range[0], range[1]));
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

    public long getNextTag() {
        return next;
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
