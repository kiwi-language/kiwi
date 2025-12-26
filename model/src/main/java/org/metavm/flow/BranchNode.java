package org.metavm.flow;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.Ref;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Setter
@Entity
public abstract class BranchNode extends Node {

    @Ref
    private LabelNode target;

    protected BranchNode(@NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code) {
        super(name, outputType, previous, code);
    }

    public Node getTarget() {
        return target;
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
