package org.metavm.entity.mocks;


import org.metavm.wire.Wire;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@org.metavm.api.Entity
@Wire(101)
public class EntityBar extends Entity {

    private final String code;

    public EntityBar(Id id, String code) {
        super(id);
        this.code = code;
    }

    @Nullable
    @Override
    public Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
