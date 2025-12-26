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

@Wire(8)
@Entity
public class KlassSourceCodeTagAssigner extends org.metavm.entity.Entity {

    public static final IndexDef<KlassSourceCodeTagAssigner> IDX_ALL_FLAGS = IndexDef.create(KlassSourceCodeTagAssigner.class,
            1, e -> List.of(Instances.booleanInstance(e.allFlags)));

    public static KlassSourceCodeTagAssigner getInstance(EntityRepository context) {
        return Objects.requireNonNull(
                context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance()),
                "ClassTagAssigner instance not found"
        );
    }

    @SuppressWarnings("unused")
    private final boolean allFlags = true;

    @Getter
    private int nextTag = 1000000;

    public KlassSourceCodeTagAssigner(Id id) {
        super(id);
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return null;
    }

    public static void initialize(IInstanceContext context) {
        var existing = context.selectFirstByKey(IDX_ALL_FLAGS, Instances.trueInstance());
        if(existing != null)
            throw new IllegalStateException("ClassTagAssigner already exists");
        context.bind(new KlassSourceCodeTagAssigner(context.allocateRootId()));
    }

    public int next() {
        return nextTag++;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
