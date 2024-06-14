package org.metavm.object.instance.rest;

import org.metavm.object.instance.InstanceKind;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

public record PrimitiveInstanceParam(
        int primitiveKind,
        Object value
) implements InstanceParam, Serializable {

    @Override
    public boolean valueEquals(InstanceParam param1, Set<String> newIds) {
        if (param1 instanceof PrimitiveInstanceParam param2) {
            return primitiveKind == param2.primitiveKind && Objects.equals(value, param2.value);
        } else
            return false;
    }

    @Override
    public int getType() {
        return InstanceKind.PRIMITIVE.code();
    }
}
