package org.metavm.util;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.TmpId;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(77)
@Entity
public class DummyAny extends org.metavm.entity.Entity {

    public DummyAny() {
        super(TmpId.random());
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
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
