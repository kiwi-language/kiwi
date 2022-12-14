package tech.metavm.object.meta;

import org.springframework.stereotype.Component;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.ReflectUtils.getFieldQualifiedName;

@Component
public class StdAllocators {

    private static final long NUM_IDS_PER_ALLOCATOR = 1000L;

    private final Map<java.lang.reflect.Type, StdAllocator> allocatorMap = new HashMap<>();
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
            return getId0(Field.class, getFieldQualifiedName(field));
        }
        if(object instanceof java.lang.reflect.Type type) {
            return getId0(ClassType.class, getTypeCode(type));
        }
        if(object instanceof Enum<?> enumConstant) {
            return getId0(enumConstant.getClass(), enumConstant.name());
        }
        if(object instanceof ModelIdentity modelIdentity) {
            return getId0(modelIdentity.type(), modelIdentity.name());
        }
        throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
    }

    public void putId(Object object, long id) {
        if(object instanceof java.lang.reflect.Field field) {
            putId0(Field.class, getFieldQualifiedName(field), id);
        }
        else if(object instanceof java.lang.reflect.Type type) {
            putId0(ClassType.class, getTypeCode(type), id);
        }
        else if(object instanceof Enum<?> enumConstant) {
            putId0(enumConstant.getClass(), enumConstant.name(), id);
        }
        else if(object instanceof ModelIdentity modelIdentity) {
            putId0(modelIdentity.type(), modelIdentity.name(), id);
        }
        else {
            throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        }
    }

    private Long getId0(Type javaType, String entityCode) {
        StdAllocator allocator = getAllocator(javaType);
        return allocator.getId(entityCode);
    }

    private void putId0(Type javaType, String entityCode, long id) {
        allocatorMap.get(javaType).putId(entityCode, id);
    }

    public long getTypeId(long id) {
        StdAllocator typeAllocator = getTypeAllocator();
        for (StdAllocator allocator : allocatorMap.values()) {
            if(allocator.contains(id)) {
                return typeAllocator.getId(allocator.getJavaType().getTypeName());
            }
        }
        throw new InternalException("Can not found typeId for id: " + id);
    }

    public Map<Type, List<Long>> allocate(Map<? extends Type, Integer> typeId2count) {
        Map<java.lang.reflect.Type, List<Long>> result = new HashMap<>();
        typeId2count.forEach((javaType, count) -> {
//            Class<?> javaType = ModelDefRegistry.getJavaType(type);
            List<Long> ids = getAllocator(javaType).allocate(count);
            result.put(javaType, ids);
        });
        return result;
    }

    private StdAllocator getAllocator(java.lang.reflect.Type javaType) {
        return allocatorMap.computeIfAbsent(javaType, this::createAllocator);
    }

    private StdAllocator createAllocator(Type javaType) {
        return new StdAllocator(
                store,
                store.createFile(javaType.getTypeName()),
                javaType,
                allocateNextBaseId(javaType)
        );
    }

    private long allocateNextBaseId(Type javaType) {
        long basedId;
        if(isArrayType(javaType)) {
            basedId = nextArrayBaseId;
            nextArrayBaseId += NUM_IDS_PER_ALLOCATOR;
        }
        else {
            basedId = nextBaseId;
            nextBaseId += NUM_IDS_PER_ALLOCATOR;
        }
        return basedId;
    }

    private boolean isArrayType(Type javaType) {
        if(javaType instanceof ParameterizedType parameterizedType) {
            return parameterizedType.getRawType() == Table.class;
        }
        else {
            return javaType == Table.class;
        }
    }

    private StdAllocator getTypeAllocator() {
        return allocatorMap.get(ClassType.class);
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
