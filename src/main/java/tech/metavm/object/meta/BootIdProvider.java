package tech.metavm.object.meta;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.ModelDefRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BootIdProvider implements EntityIdProvider  {

    private final StdAllocators allocators;

    public BootIdProvider(StdAllocators allocators) {
        this.allocators = allocators;
    }

    @Override
    public long getTypeId(long id) {
        return allocators.getTypeId(id);
    }

    @Override
    public Map<Type, List<Long>> allocate(long tenantId, Map<Type, Integer> typeId2count) {
        Map<java.lang.reflect.Type, Integer> javaType2count = new HashMap<>();
        typeId2count.forEach((type, count) ->
                javaType2count.put(ModelDefRegistry.getJavaType(type), count)
        );
        Map<java.lang.reflect.Type, List<Long>> javaType2ids =  allocators.allocate(javaType2count);
        Map<Type, List<Long>> result = new HashMap<>();
        javaType2ids.forEach((javaType, ids) ->
                result.put(ModelDefRegistry.getType(javaType), ids)
        );
        return result;
    }
}
