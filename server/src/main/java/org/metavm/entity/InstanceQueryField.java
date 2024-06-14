package org.metavm.entity;

import org.metavm.common.ErrorCode;
import org.metavm.object.instance.InstanceFactory;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.rest.InstanceQueryFieldDTO;
import org.metavm.object.type.Field;
import org.metavm.util.BusinessException;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;

public record InstanceQueryField(
        Field field,
        @Nullable Instance value,
        @Nullable Instance min,
        @Nullable Instance max
) {

    public InstanceQueryField {
        if (field == null || value == null && min == null && max == null)
            throw new BusinessException(ErrorCode.ILLEGAL_SEARCH_CONDITION);
    }

    public static InstanceQueryField create(Field field, Instance value) {
        return new InstanceQueryField(field, value, null, null);
    }

    public static InstanceQueryField create(Field field, Instance min, Instance max) {
        return new InstanceQueryField(field, null, min, max);
    }

    public static InstanceQueryField create(InstanceQueryFieldDTO queryFieldDTO, IEntityContext context) {
        var field = context.getField(queryFieldDTO.fieldId());
        return new InstanceQueryField(
                field,
                NncUtils.get(queryFieldDTO.value(), v ->
                        InstanceFactory.resolveValue(v, field.getType(), context)),
                NncUtils.get(queryFieldDTO.min(), v ->
                        InstanceFactory.resolveValue(v, field.getType(), context)),
                NncUtils.get(queryFieldDTO.max(), v ->
                        InstanceFactory.resolveValue(v, field.getType(), context))
        );
    }

}
