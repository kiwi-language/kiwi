package org.metavm.mocks;

import org.metavm.api.Entity;
import org.metavm.api.EntityField;
import org.metavm.wire.Wire;
import org.metavm.wire.Parent;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(86)
@Entity
public class Bar extends org.metavm.entity.Entity {
    @Parent
    private final @Nullable Foo foo;
    @EntityField(asTitle = true)
    private final String code;

    public Bar(Id id,  @Nullable Foo foo, String code) {
        super(id);
        this.foo = foo;
        this.code = code;
    }

    public String code() {
        return code;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return foo;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
