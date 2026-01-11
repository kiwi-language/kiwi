package org.manul.entity.mocks;


import org.manul.wire.Wire;
import org.manul.entity.Entity;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@org.manul.api.Entity
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
