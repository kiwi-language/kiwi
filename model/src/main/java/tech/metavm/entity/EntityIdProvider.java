package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.object.type.Type;

import java.util.List;
import java.util.Map;

public interface EntityIdProvider {

    TypeId getTypeId(Id id);

    Map<Type, List<Long>> allocate(Id appId, Map<Type, Integer> typeId2count);

    default Long allocateOne(Id appId, Type type) {
        return allocate(appId, Map.of(type, 1)).values().iterator().next().get(0);
    }

}
