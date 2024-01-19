package tech.metavm.object.instance.rest;

import tech.metavm.object.instance.InstanceKind;
import tech.metavm.util.NncUtils;

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

    @Override
    public boolean valueEquals(InstanceParam that) {
        if (that instanceof ArrayInstanceParam thatArrayInstanceParam) {
            return elementAsChild == thatArrayInstanceParam.elementAsChild
                    && NncUtils.listEquals(elements, thatArrayInstanceParam.elements, FieldValue::valueEquals);
        } else
            return false;
    }

}
