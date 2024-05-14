package tech.metavm.entity;

import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Map;

public interface EntityIdProvider {

    Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count);

    default Long allocateOne(long appId, Type type) {
        return allocate(appId, Map.of(type, 1)).values().iterator().next().get(0);
    }

}
