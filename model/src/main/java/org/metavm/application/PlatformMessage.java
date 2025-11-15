package org.metavm.application;

import lombok.Getter;
import org.metavm.wire.Wire;
import org.metavm.entity.Entity;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@org.metavm.api.Entity
@Wire(5)
public class PlatformMessage extends Entity {

    private final String title;

    public PlatformMessage(Id id, String title) {
        super(id);
        this.title = title;
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
