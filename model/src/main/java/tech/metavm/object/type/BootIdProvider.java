package tech.metavm.object.type;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.util.NncUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BootIdProvider implements EntityIdProvider  {

    private final StdAllocators allocators;
    private final Function<Type, java.lang.reflect.Type> getJavaType;

    public BootIdProvider(StdAllocators allocators) {
        this(allocators, ModelDefRegistry::getJavaType);
    }

    public BootIdProvider(StdAllocators allocators,
                          Function<Type, java.lang.reflect.Type> getJavaType) {
        this.allocators = allocators;
        this.getJavaType = getJavaType;
    }

    @Override
    public TypeId getTypeId(Id id) {
        return allocators.getTypeId(id.getPhysicalId());
    }

    @Override
    public Map<Type, List<Long>> allocate(Id appId, Map<Type, Integer> typeId2count) {
        Map<java.lang.reflect.Type, Integer> javaType2count = new HashMap<>();
        Map<java.lang.reflect.Type, Type> javaType2type = new HashMap<>();
        typeId2count.forEach((type, count) -> {
            java.lang.reflect.Type javaType = NncUtils.requireNonNull(getJavaType.apply(type));
            javaType2type.put(javaType, type);
            javaType2count.put(javaType, count);
        });
        Map<java.lang.reflect.Type, List<Long>> javaType2ids =  allocators.allocate(javaType2count);
        Map<Type, List<Long>> result = new HashMap<>();
        javaType2ids.forEach((javaType, ids) ->
                result.put(javaType2type.get(javaType), ids)
        );
        return result;
    }

    public Long getId(Object model) {
        return allocators.getId(model);
    }

}
