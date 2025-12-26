package org.metavm.mocks;

import lombok.Getter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Wire(92)
@Entity
public class Qux extends org.metavm.entity.Entity {

    public static final IndexDef<Qux> IDX_AMOUNT = IndexDef.create(Qux.class,
            1, qux -> List.of(Instances.longInstance(qux.amount)));

    private final long amount;

    public Qux(Id id, long amount) {
        super(id);
        this.amount = amount;
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
