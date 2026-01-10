package org.manul.mocks;

import lombok.Getter;
import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Wire(92)
@Entity
public class Qux extends org.manul.entity.Entity {

    public static final IndexDef<Qux> IDX_AMOUNT = IndexDef.create(Qux.class,
            1, qux -> List.of(Instances.longInstance(qux.amount)));

    private final long amount;

    public Qux(Id id, long amount) {
        super(id);
        this.amount = amount;
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
