package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;

public record PrimitiveParamDTO(
        int primitiveKind,
        Object value
) implements InstanceParamDTO {
    @Override
    public int getType() {
        return InstanceKind.PRIMITIVE.code();
    }
}
