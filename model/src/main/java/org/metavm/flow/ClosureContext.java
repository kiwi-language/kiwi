package org.metavm.flow;

import org.metavm.object.instance.core.Value;

public interface ClosureContext {

    Value getContextSlot(int contextIndex, int slotIndex);

    void setContextSlot(int contextIndex, int slotIndex, Value value);

}
