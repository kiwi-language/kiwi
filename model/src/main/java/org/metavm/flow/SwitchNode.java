package org.metavm.flow;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.Ref;
import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Entity
public abstract class SwitchNode extends Node {

    @Setter
    @Getter
    @Ref
    protected LabelNode defaultTarget;

    @Ref
    protected final List<LabelNode> targets = new ArrayList<>();

    protected SwitchNode(@NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code) {
        super(name, outputType, previous, code);
    }

    public void addTarget(LabelNode target) {
        targets.add(target);
    }

    public void setTargets(List<LabelNode> targets) {
        this.targets.clear();
        this.targets.addAll(targets);
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
