package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;

import java.io.Serializable;
import java.util.List;

public record ArrayInstanceParam(
        boolean elementAsChild,
        List<FieldValue> elements
) implements InstanceParam, Serializable {
    @Override
    public int getType() {
        return InstanceKind.ARRAY.code();
    }
}
