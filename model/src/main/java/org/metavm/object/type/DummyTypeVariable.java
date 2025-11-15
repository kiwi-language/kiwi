package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.wire.Wire;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.TmpId;

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
