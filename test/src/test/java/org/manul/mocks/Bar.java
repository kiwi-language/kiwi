package org.manul.mocks;

import org.manul.api.Entity;
import org.manul.api.EntityField;
import org.manul.wire.Wire;
import org.manul.wire.Parent;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Wire(86)
@Entity
public class Bar extends org.manul.entity.Entity {
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
    public org.manul.entity.Entity getParentEntity() {
        return foo;
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
