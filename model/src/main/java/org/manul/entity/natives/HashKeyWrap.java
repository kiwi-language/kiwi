package org.manul.entity.natives;

import org.manul.object.instance.core.Value;
import org.manul.util.Instances;

public record HashKeyWrap(Value value, CallContext callContext) {

    @Override
    public int hashCode() {
        return Instances.hashCode(value, callContext);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof HashKeyWrap that && Instances.equals(value, that.value, callContext);
    }
}
