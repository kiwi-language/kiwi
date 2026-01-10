package org.manul.object.type;

import lombok.Getter;
import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.EntityRepository;
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

@Wire(64)
@Entity
public class KlassTagAssigner extends org.manul.entity.Entity {

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
