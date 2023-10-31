package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;

import java.util.List;

public record ArrayParamDTO (
        boolean elementAsChild,
        List<FieldValue> elements
) implements InstanceParamDTO{
    @Override
    public int getType() {
        return InstanceKind.ARRAY.code();
    }
}
