package org.manul.util;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.TmpId;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(77)
@Entity
public class DummyAny extends org.manul.entity.Entity {

    public DummyAny() {
        super(TmpId.random());
    }

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
