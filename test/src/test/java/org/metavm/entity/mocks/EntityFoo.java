package org.metavm.entity.mocks;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.IndexDef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.util.Instances;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

@Wire(91)
@Entity
public class EntityFoo extends org.metavm.entity.Entity {

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
    public org.metavm.entity.Entity getParentEntity() {
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
