package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public abstract class JumpNode extends NodeRT {

    private NodeRT target = this;

    protected JumpNode(Long tmpId, @NotNull String name, @Nullable Type outputType, @Nullable NodeRT previous, @NotNull ScopeRT scope) {
        super(tmpId, name, outputType, previous, scope);
    }

    public NodeRT getTarget() {
        return target;
    }

    public void setTarget(NodeRT target) {
        this.target = target;
    }
}
