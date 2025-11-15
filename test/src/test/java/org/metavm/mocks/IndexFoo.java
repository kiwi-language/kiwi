package org.metavm.mocks;

import lombok.Getter;
import lombok.Setter;
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

@Setter
@Getter
@Wire(93)
@Entity
public class IndexFoo extends org.metavm.entity.Entity {

    public static final IndexDef<IndexFoo> IDX_STATE = IndexDef.create(IndexFoo.class,
            1, indexFoo -> List.of(Instances.intInstance(indexFoo.state.code())));
    public static final IndexDef<IndexFoo> IDX_CODE = IndexDef.create(IndexFoo.class,
            1, indexFoo -> List.of(Instances.intInstance(indexFoo.code)));

    private FooState state = FooState.STATE1;
    private int code;

    public IndexFoo(Id id) {
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
