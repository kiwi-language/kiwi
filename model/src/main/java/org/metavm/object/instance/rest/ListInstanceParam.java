package org.metavm.object.instance.rest;

import org.metavm.object.instance.InstanceKind;
import org.metavm.util.Utils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public record ListInstanceParam(
        boolean elementAsChild,
        List<FieldValue> elements
) implements InstanceParam, Serializable {

    @Override
    public int getType() {
        return InstanceKind.LIST.code();
    }

    @Override
    public boolean valueEquals(InstanceParam that, Set<String> newIds) {
        if (that instanceof ListInstanceParam thatArrayInstanceParam) {
            return elementAsChild == thatArrayInstanceParam.elementAsChild
                    && Utils.listEquals(elements, thatArrayInstanceParam.elements, (fieldValue, that1) -> fieldValue.valueEquals(that1, newIds));
        } else
            return false;
    }

}
