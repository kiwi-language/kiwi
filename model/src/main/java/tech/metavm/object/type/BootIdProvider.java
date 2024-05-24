package tech.metavm.object.type;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BootIdProvider implements EntityIdProvider {

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
    public Map<Type, List<Long>> allocate(long appId, Map<Type, Integer> typeId2count) {
        Map<java.lang.reflect.Type, Integer> javaType2count = new HashMap<>();
        Map<java.lang.reflect.Type, Type> javaType2type = new HashMap<>();
        typeId2count.forEach((type, count) -> {
            java.lang.reflect.Type javaType = NncUtils.requireNonNull(getJavaType.apply(type));
            javaType2type.put(javaType, type);
            javaType2count.put(javaType, count);
        });
        var javaType2ids = allocators.allocate(javaType2count);
        Map<Type, List<Long>> result = new HashMap<>();
        javaType2ids.forEach((javaType, ids) -> {
            var type = javaType2type.get(javaType);
            result.put(type, ids);
        });
        return result;
    }

    public Id getId(Object model) {
        return allocators.getId(model);
    }

    public @Nullable Long getNextNodeId(Object model) {
        return allocators.getNextNodeId(model);
    }

}
