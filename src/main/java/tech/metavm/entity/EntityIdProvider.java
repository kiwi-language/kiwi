package tech.metavm.entity;

import tech.metavm.object.meta.Type;

import java.util.List;
import java.util.Map;

public interface EntityIdProvider {

    long getTypeId(long id);

    Map<Type, List<Long>> allocate(long tenantId, Map<Type, Integer> typeId2count);

    default Long allocateOne(long tenantId, Type type) {
        return allocate(tenantId, Map.of(type, 1)).values().iterator().next().get(0);
    }

}
