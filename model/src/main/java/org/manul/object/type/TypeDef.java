package org.manul.object.type;

import org.jetbrains.annotations.NotNull;
import org.manul.api.Entity;
import org.manul.entity.AttributedElement;
import org.manul.wire.Wire;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;

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
