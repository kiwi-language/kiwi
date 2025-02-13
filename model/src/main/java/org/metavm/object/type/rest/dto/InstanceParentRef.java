package org.metavm.object.type.rest.dto;

import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.Field;

import javax.annotation.Nullable;

public record InstanceParentRef(
        Reference parent,
        @Nullable Field field
) {

//    @Nullable
    public static InstanceParentRef ofArray(Reference reference) {
        return new InstanceParentRef(reference, null);
    }

    public static InstanceParentRef ofObject(Reference reference, Field field) {
        return new InstanceParentRef(reference, field);
    }


}
