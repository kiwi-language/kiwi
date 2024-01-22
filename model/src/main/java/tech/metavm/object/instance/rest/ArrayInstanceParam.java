package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;
import tech.metavm.util.NncUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public record ArrayInstanceParam(
        boolean elementAsChild,
        List<FieldValue> elements
) implements InstanceParam, Serializable {
    @Override
    public int getType() {
        return InstanceKind.ARRAY.code();
    }

    @Override
    public boolean valueEquals(InstanceParam that, Set<String> newIds) {
        if (that instanceof ArrayInstanceParam thatArrayInstanceParam) {
            return elementAsChild == thatArrayInstanceParam.elementAsChild
                    && NncUtils.listEquals(elements, thatArrayInstanceParam.elements, (fieldValue, that1) -> fieldValue.valueEquals(that1, newIds));
        } else
            return false;
    }

}
