package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;

import java.io.Serializable;

public record PrimitiveInstanceParam(
        int primitiveKind,
        Object value
) implements InstanceParam, Serializable {
    @Override
    public int getType() {
        return InstanceKind.PRIMITIVE.code();
    }
}
