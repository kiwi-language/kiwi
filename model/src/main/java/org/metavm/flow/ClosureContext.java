package org.metavm.flow;

import org.metavm.object.instance.core.Value;

import javax.annotation.Nullable;
import java.util.Objects;

public class ClosureContext {

    private final @Nullable ClosureContext parent;
    private final Value[] slots;

    public ClosureContext(@Nullable ClosureContext parent, Value[] slots) {
        this.parent = parent;
        this.slots = slots;
    }

    public Value get(int contextIndex, int slotIndex) {
        assert contextIndex >= 0;
        if(contextIndex == 0)
            return slots[slotIndex];
        else
            return Objects.requireNonNull(parent).get(contextIndex - 1, slotIndex);
    }

    public void set(int contextIndex, int slotIndex, Value value) {
        assert contextIndex >= 0;
        if(contextIndex == 0)
            slots[slotIndex] = value;
        else
            Objects.requireNonNull(parent).set(contextIndex - 1, slotIndex, value);
    }

}
