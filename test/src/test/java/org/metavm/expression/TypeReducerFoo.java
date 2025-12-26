package org.metavm.expression;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(80)
@Entity
public class TypeReducerFoo extends org.metavm.entity.Entity {

    @Nullable
    public String code;

    public int amount;

    public TypeReducerFoo(Id id) {
        super(id);
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
