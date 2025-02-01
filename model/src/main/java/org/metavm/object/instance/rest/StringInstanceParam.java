package org.metavm.object.instance.rest;

import org.metavm.object.instance.InstanceKind;

import java.io.Serializable;
import java.util.Set;

public record StringInstanceParam(
        String value
) implements InstanceParam, Serializable {

    @Override
    public boolean valueEquals(InstanceParam param1, Set<String> newIds) {
        return param1 instanceof StringInstanceParam that && value.equals(that.value);
    }

    @Override
    public int getType() {
        return InstanceKind.STRING.code();
    }
}
