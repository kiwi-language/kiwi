package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public abstract class JumpNode extends Node {

    private Node target = this;

    protected JumpNode(Long tmpId, @NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code) {
        super(tmpId, name, outputType, previous, code);
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }
}
