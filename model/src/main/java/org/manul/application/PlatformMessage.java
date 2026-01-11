package org.manul.application;

import lombok.Getter;
import org.manul.wire.Wire;
import org.manul.entity.Entity;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@org.manul.api.Entity
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
