package org.manul.mocks;

import lombok.Getter;
import lombok.Setter;
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

@Setter
@Getter
@Wire(93)
@Entity
public class IndexFoo extends org.manul.entity.Entity {

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
