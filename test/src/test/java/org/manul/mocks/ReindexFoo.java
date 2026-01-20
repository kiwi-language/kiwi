package org.manul.mocks;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.manul.compiler.util.List;
import org.manul.entity.Entity;
import org.manul.entity.IndexDef;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Instances;
import org.manul.wire.Wire;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(204)
@org.manul.api.Entity
public class ReindexFoo extends Entity  {

    public static boolean indexOn;

    public static final IndexDef<ReindexFoo> NAME_IDX = IndexDef.create(ReindexFoo.class, 1,
            foo -> List.of(Instances.stringInstance(indexOn ? foo.name : "<unnamed>")));

    @Getter
    @Setter
    public String name;

    public ReindexFoo(@NotNull Id id, String name) {
        super(id);
        this.name = name;
    }

    @Nullable
    @Override
    public Entity getParentEntity() {
        return null;
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {

    }

    @Override
    public void forEachReference(Consumer<Reference> action) {

    }

}
