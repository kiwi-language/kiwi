package org.metavm.entity;

import org.metavm.common.ErrorCode;
import org.metavm.object.instance.core.Id;
import org.metavm.util.BusinessException;

import java.util.List;

public record EntityQuery<T extends Entity>(
        Class<T> entityType,
        List<SearchField<? super T>> searchFields,
        boolean includeBuiltin,
        int page,
        int pageSize,
        List<EntityQueryField<? super T>> fields,
        List<Id> newlyCreatedIds,
        List<String> excluded
        ) {


        public EntityQuery {
                if (page <= 0 || pageSize <= 0)
                        throw new BusinessException(ErrorCode.ILLEGAL_QUERY);
        }

}
