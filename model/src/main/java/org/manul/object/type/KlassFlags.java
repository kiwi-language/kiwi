package org.manul.object.type;

import lombok.Getter;
import lombok.Setter;
import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(59)
@Entity(since = 1)
public class KlassFlags extends org.manul.entity.Entity {
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
    public org.manul.entity.Entity getParentEntity() {
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
