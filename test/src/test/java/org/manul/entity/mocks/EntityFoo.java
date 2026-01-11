package org.manul.entity.mocks;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Wire(91)
@Entity
public class EntityFoo extends org.manul.entity.Entity {

    public static final IndexDef<EntityFoo> idxName
            = IndexDef.create(EntityFoo.class, 1, f -> List.of(Instances.stringInstance(f.name)));

    public static final IndexDef<EntityFoo> idxBar
            = IndexDef.create(EntityFoo.class, 1 , f -> List.of(f.bar));

    public String name;
    private final Reference bar;
    @Nullable
    private ValueBaz baz;

    public EntityFoo(Id id, String name, EntityBar bar) {
        super(id);
        this.name = name;
        this.bar = bar.getReference();
    }

    public EntityBar getBar() {
        return (EntityBar) bar.get();
    }

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        action.accept(bar);
        if (baz != null) baz.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
