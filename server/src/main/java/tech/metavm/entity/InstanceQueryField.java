package tech.metavm.entity;

import tech.metavm.common.ErrorCode;
import tech.metavm.expression.BinaryOperator;
import tech.metavm.object.instance.InstanceFactory;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.rest.InstanceQueryFieldDTO;
import tech.metavm.object.type.Field;
import tech.metavm.util.BusinessException;
import tech.metavm.util.NncUtils;

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

    public static InstanceQueryField create(InstanceQueryFieldDTO queryFieldDTO, IInstanceContext context) {
        var entityContext = context.getEntityContext();
        var field = entityContext.getField(queryFieldDTO.fieldId());
        return new InstanceQueryField(
                field,
                NncUtils.get(queryFieldDTO.value(), v ->
                        InstanceFactory.resolveValue(v, field.getType(), entityContext)),
                NncUtils.get(queryFieldDTO.min(), v ->
                        InstanceFactory.resolveValue(v, field.getType(), entityContext)),
                NncUtils.get(queryFieldDTO.max(), v ->
                        InstanceFactory.resolveValue(v, field.getType(), entityContext))
        );
    }

}
