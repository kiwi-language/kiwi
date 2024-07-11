package org.metavm.object.type.rest.dto;

import org.metavm.object.instance.core.InstanceReference;
import org.metavm.object.type.Field;

import javax.annotation.Nullable;

public record InstanceParentRef(
        InstanceReference parent,
        @Nullable Field field
) {

//    @Nullable
    public static InstanceParentRef ofArray(InstanceReference reference) {
        return new InstanceParentRef(reference, null);
    }

    @Nullable
    public static InstanceParentRef ofObject(InstanceReference reference, Field field) {
        return field.isChild() ? new InstanceParentRef(reference, field) : null;
    }


}
