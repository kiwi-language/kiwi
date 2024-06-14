package org.metavm.object.type.rest.dto;

import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.type.Field;

import javax.annotation.Nullable;

public record InstanceParentRef(
        DurableInstance parent,
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
