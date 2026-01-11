package org.manul.entity;

import org.manul.common.ErrorCode;
import org.manul.object.instance.core.Id;
import org.manul.object.type.Field;
import org.manul.object.type.Klass;
import org.manul.util.BusinessException;

import javax.annotation.Nullable;
import java.util.List;

public record InstanceQuery(
        Klass klass,
        @Nullable String searchText,
        @Nullable String expression,
        List<Field> searchFields,
        boolean includeBuiltin,
        boolean includeSubTypes,
        int page,
        int pageSize,
        List<InstanceQueryField> fields,
        List<Id> createdIds,
        List<Id> excludedIds
        ) {


        public InstanceQuery {
               if (page <= 0 || pageSize <= 0)
                       throw new BusinessException(ErrorCode.ILLEGAL_QUERY);
        }
}
