package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public abstract class VariableAccessNode extends NodeRT {

    protected final int index;

    protected VariableAccessNode(Long tmpId, @NotNull String name, @Nullable String code, @Nullable Type outputType, @Nullable NodeRT previous, @NotNull ScopeRT scope, int index) {
        super(tmpId, name, code, outputType, previous, scope);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
