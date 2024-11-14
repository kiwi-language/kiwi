package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.Type;

import javax.annotation.Nullable;

public abstract class VariableAccessNode extends Node {

    protected final int index;

    protected VariableAccessNode(@NotNull String name, @Nullable Type outputType, @Nullable Node previous, @NotNull Code code, int index) {
        super(name, outputType, previous, code);
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
