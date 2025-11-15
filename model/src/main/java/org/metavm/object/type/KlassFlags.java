package org.metavm.object.type;

import lombok.Getter;
import lombok.Setter;
import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(59)
@Entity(since = 1)
public class KlassFlags extends org.metavm.entity.Entity {
    @Setter
    @Getter
    private boolean flag1;
    private final Klass klass;

    public KlassFlags(Klass klass) {
        super(klass.getRoot().nextChildId());
        this.klass = klass;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return klass;
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
