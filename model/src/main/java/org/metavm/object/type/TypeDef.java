package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.AttributedElement;
import org.metavm.wire.Wire;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;

import java.util.function.Consumer;

@Entity
@Wire(21)
public abstract class TypeDef extends AttributedElement implements ITypeDef {

    public TypeDef(Id id) {
        super(id);
    }

    public abstract @NotNull Type getType();

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
    }

}
