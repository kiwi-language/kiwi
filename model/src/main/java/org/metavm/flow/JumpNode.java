package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public abstract class JumpNode extends Node {

    private LabelNode target;

    protected JumpNode(@NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code) {
        super(name, outputType, previous, code);
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(LabelNode target) {
        this.target = target;
    }
}
