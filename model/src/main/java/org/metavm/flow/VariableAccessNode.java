package org.metavm.flow;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Getter
@Entity
public abstract class VariableAccessNode extends Node {

    protected final int index;

    protected VariableAccessNode(@NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code, int index) {
        super(name, outputType, previous, code);
        this.index = index;
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
