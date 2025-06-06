package org.metavm.entity;

import org.metavm.common.ErrorCode;
import org.metavm.util.BusinessException;
import org.metavm.util.Utils;

import java.util.List;

public record EntityQuery<T extends Entity>(
        Class<T> entityType,
        List<SearchField<? super T>> searchFields,
        boolean includeBuiltin,
        int page,
        int pageSize,
        List<EntityQueryField<? super T>> fields,
        List<String> newlyCreated,
        List<String> excluded
        ) {


        public EntityQuery {
                if (page <= 0 || pageSize <= 0)
                        throw new BusinessException(ErrorCode.ILLEGAL_QUERY);
        }

        public boolean matches(T entity) {
                return !excluded.contains(entity.getStringId()) &&
                        Utils.allMatch(fields, f -> f.matches(entity));
        }

}
