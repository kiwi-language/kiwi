package org.manul.object.type;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.entity.DummyGenericDeclaration;
import org.manul.entity.ElementVisitor;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.object.instance.core.TmpId;

import java.util.function.Consumer;

@Wire(25)
@Entity(ephemeral = true)
public class DummyTypeVariable extends TypeVariable {

    public static final DummyTypeVariable instance = new DummyTypeVariable();

    private DummyTypeVariable() {
        super(TmpId.random(), "Dummy", DummyGenericDeclaration.INSTANCE);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        throw new UnsupportedOperationException();
    }

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
