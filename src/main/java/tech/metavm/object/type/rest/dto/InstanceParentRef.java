package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.core.ArrayInstance;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Field;

import javax.annotation.Nullable;

public record InstanceParentRef(
        Instance parent,
        @Nullable Field field
) {

    @Nullable
    public static InstanceParentRef ofArray(ArrayInstance array) {
        return array.isChildArray() ? new InstanceParentRef(array, null) : null;
    }

    @Nullable
    public static InstanceParentRef ofObject(ClassInstance object, Field field) {
        return field.isChild() ? new InstanceParentRef(object, field) : null;
    }


}
