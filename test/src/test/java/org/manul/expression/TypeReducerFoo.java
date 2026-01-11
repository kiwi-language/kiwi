package org.manul.expression;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(80)
@Entity
public class TypeReducerFoo extends org.manul.entity.Entity {

    @Nullable
    public String code;

    public int amount;

    public TypeReducerFoo(Id id) {
        super(id);
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
