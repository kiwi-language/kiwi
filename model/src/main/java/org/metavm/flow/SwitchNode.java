package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.ReadWriteArray;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;
import java.util.List;

public abstract class SwitchNode extends Node {

    protected Node defaultTarget = this;

    @ChildEntity
    protected final ReadWriteArray<Node> targets = addChild(new ReadWriteArray<>(Node.class), "targets");

    protected SwitchNode(@NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code) {
        super(name, outputType, previous, code);
    }

    public Node getDefaultTarget() {
        return defaultTarget;
    }

    public void setDefaultTarget(Node defaultTarget) {
        this.defaultTarget = defaultTarget;
    }

    public void addTarget(Node target) {
        targets.add(target);
    }

    public void setTargets(List<Node> targets) {
        this.targets.reset(targets);
    }

}
