package org.metavm.entity;

import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Field;
import org.metavm.util.BusinessException;

import javax.annotation.Nullable;

public record InstanceQueryField(
        Field field,
        @Nullable Value value,
        @Nullable Value min,
        @Nullable Value max
) {

    public InstanceQueryField {
        if (field == null || value == null && min == null && max == null)
            throw new BusinessException(ErrorCode.ILLEGAL_SEARCH_CONDITION);
    }

    public static InstanceQueryField create(Field field, Value value) {
        return new InstanceQueryField(field, value, null, null);
    }

    public static InstanceQueryField create(Field field, Value min, Value max) {
        return new InstanceQueryField(field, null, min, max);
    }

}
