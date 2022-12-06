package tech.metavm.object.meta;

import org.springframework.stereotype.Component;
import tech.metavm.entity.ArrayIdentifier;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.ReflectUtils.getQualifiedFieldName;

@Component
public class StdAllocators {

    private static final long NUM_IDS_PER_ALLOCATOR = 1000L;

    private final Map<Class<?>, StdAllocator> allocatorMap = new HashMap<>();
    private long nextBaseId = 10000L;
    private long nextArrayBaseId = IdConstants.ARRAY_REGION_BASE + 10000L;
    private final AllocatorStore store;

    public StdAllocators(AllocatorStore store) {
        this.store = store;
        for (String fileName : store.getFileNames()) {
            StdAllocator allocator = new StdAllocator(store, fileName);
            allocatorMap.put(allocator.getJavaType(), allocator);
            if(allocator.isArray()) {
                nextArrayBaseId = Math.max(nextArrayBaseId, allocator.getNextId() + NUM_IDS_PER_ALLOCATOR);
            }
            else {
                nextBaseId = Math.max(nextBaseId, allocator.getNextId() + NUM_IDS_PER_ALLOCATOR);
            }
        }
    }

    public Long getId(Object object) {
        if(object instanceof java.lang.reflect.Field field) {
            return getId0(Field.class, getQualifiedFieldName(field));
        }
        if(object instanceof java.lang.reflect.Type type) {
            return getId0(Type.class, getTypeCode(type));
        }
        if(object instanceof Enum<?> enumConstant) {
            return getId0(enumConstant.getClass(), enumConstant.name());
        }
        if(object instanceof ArrayIdentifier arrayIdentifier) {
            return getId0(Table.class, arrayIdentifier.name());
        }
        throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
    }

    public void putId(Object object, long id) {
        if(object instanceof java.lang.reflect.Field field) {
            putId0(Field.class, getQualifiedFieldName(field), id);
        }
        else if(object instanceof java.lang.reflect.Type type) {
            putId0(Type.class, getTypeCode(type), id);
        }
        else if(object instanceof Enum<?> enumConstant) {
            putId0(enumConstant.getClass(), enumConstant.name(), id);
        }
        else if(object instanceof ArrayIdentifier arrayIdentifier) {
            putId0(Table.class, arrayIdentifier.name(), id);
        }
        else {
            throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        }
    }

    private Long getId0(Class<?> entityType, String entityCode) {
        StdAllocator allocator = getAllocator(entityType);
        return allocator.getId(entityCode);
    }

    private void putId0(Class<?> entityType, String entityCode, long id) {
        allocatorMap.get(entityType).putId(entityCode, id);
    }

    public long getTypeId(long id) {
        StdAllocator typeAllocator = getTypeAllocator();
        for (StdAllocator allocator : allocatorMap.values()) {
            if(allocator.contains(id)) {
                return typeAllocator.getId(allocator.getJavaType().getName());
            }
        }
        throw new InternalException("Can not found typeId for id: " + id);
    }

    public Map<Class<?>, List<Long>> allocate(Map<Class<?>, Integer> typeId2count) {
        Map<Class<?>, List<Long>> result = new HashMap<>();
        typeId2count.forEach((javaType, count) -> {
//            Class<?> javaType = ModelDefRegistry.getJavaType(type);
            List<Long> ids = getAllocator(javaType).allocate(count);
            result.put(javaType, ids);
        });
        return result;
    }

    private StdAllocator getAllocator(Class<?> javaType) {
        return allocatorMap.computeIfAbsent(javaType, this::createAllocator);
    }

    private StdAllocator createAllocator(Class<?> javaType) {
        return new StdAllocator(
                store,
                store.createFile(javaType.getSimpleName()),
                javaType,
                allocateNextBaseId(javaType)
        );
    }

    private long allocateNextBaseId(Class<?> javaType) {
        if(javaType == Table.class) {
            long basedId = nextArrayBaseId;
            nextArrayBaseId += NUM_IDS_PER_ALLOCATOR;
            return basedId;
        }
        else {
            long basedId = nextBaseId;
            nextBaseId += NUM_IDS_PER_ALLOCATOR;
            return basedId;
        }
    }

    private StdAllocator getTypeAllocator() {
        return allocatorMap.get(Type.class);
    }

    private String getTypeCode(java.lang.reflect.Type type) {
        if(type instanceof Class<?> klass) {
            return klass.getName();
        }
        if(type instanceof ParameterizedType pType) {
            return getTypeCode(pType.getRawType()) + "<" +
                    NncUtils.join(pType.getActualTypeArguments(), this::getTypeCode) + ">";
        }
        if(type instanceof WildcardType wildcardType) {
            if(ReflectUtils.isAllWildCardType(wildcardType)) {
                return "?";
            }
        }
        throw new InternalException("Can not get code for type: " + type);
    }

    public void save() {
        allocatorMap.values().forEach(StdAllocator::save);
    }

}
