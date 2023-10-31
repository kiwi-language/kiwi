package tech.metavm.object.meta;

import org.springframework.stereotype.Component;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.util.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.ReflectUtils.getFieldQualifiedName;

@Component
public class StdAllocators {

    private static final long NUM_IDS_PER_ALLOCATOR = 10000L;

    private final Map<java.lang.reflect.Type, StdAllocator> allocatorMap = new HashMap<>();
    private long nextBaseId = 10000L;
    private long nextReadWriteArrayBaseId = IdConstants.READ_WRITE_ARRAY_REGION_BASE + 10000L;
    private long nextChildArrayBaseId = IdConstants.CHILD_ARRAY_REGION_BASE + 10000L;
    private long nextReadOnlyArrayBaseId = IdConstants.READ_ONLY_ARRAY_REGION_BASE + 10000L;
    private final AllocatorStore store;

    public StdAllocators(AllocatorStore store) {
        this.store = store;
        for (String fileName : store.getFileNames()) {
            StdAllocator allocator = new StdAllocator(store, fileName);
            allocatorMap.put(allocator.getJavaType(), allocator);
            if (allocator.isChildArray()) {
                nextChildArrayBaseId = Math.max(nextChildArrayBaseId, allocator.getNextId() + NUM_IDS_PER_ALLOCATOR);
            } else if (allocator.isReadWriteArray()) {
                nextReadWriteArrayBaseId = Math.max(nextReadWriteArrayBaseId, allocator.getNextId() + NUM_IDS_PER_ALLOCATOR);
            } else if (allocator.isReadonlyArray()) {
                nextReadOnlyArrayBaseId = Math.max(nextReadOnlyArrayBaseId, allocator.getNextId() + NUM_IDS_PER_ALLOCATOR);
            } else {
                nextBaseId = Math.max(nextBaseId, allocator.getNextId() + NUM_IDS_PER_ALLOCATOR);
            }
        }
    }

    public Long getId(Object object) {
        if (object instanceof java.lang.reflect.Field field) {
            return getId0(Field.class, getFieldQualifiedName(field));
        }
        if (object instanceof java.lang.reflect.Type type) {
            return getId0(ClassType.class, getTypeCode(type));
        }
        if (object instanceof Enum<?> enumConstant) {
            return getId0(enumConstant.getClass(), enumConstant.name());
        }
        if (object instanceof ModelIdentity modelIdentity) {
            return getId0(modelIdentity.type(), modelIdentity.name());
        }
        throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
    }

    public void putId(Object object, long id) {
        if (object instanceof java.lang.reflect.Field field) {
            putId0(Field.class, getFieldQualifiedName(field), id);
        } else if (object instanceof java.lang.reflect.Type type) {
            putId0(ClassType.class, getTypeCode(type), id);
        } else if (object instanceof Enum<?> enumConstant) {
            putId0(enumConstant.getClass(), enumConstant.name(), id);
        } else if (object instanceof ModelIdentity modelIdentity) {
            putId0(modelIdentity.type(), modelIdentity.name(), id);
        } else {
            throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        }
    }

    public StdAllocator getAllocatorById(long id) {
        return NncUtils.find(allocatorMap.values(), a -> a.contains(id));
    }

    private Long getId0(Type javaType, String entityCode) {
        StdAllocator allocator = getAllocator(javaType);
        return allocator.getId(entityCode);
    }

    private void putId0(Type javaType, String entityCode, long id) {
        allocatorMap.get(javaType).putId(entityCode, id);
    }

    public long getTypeId(long id) {
        StdAllocator classTypeAllocator = allocatorMap.get(ClassType.class);
        StdAllocator arrayTypeAllocator = allocatorMap.get(ArrayType.class);
        for (StdAllocator allocator : allocatorMap.values()) {
            if (allocator.contains(id)) {
                if (isMetaArray(allocator.getJavaType())) {
                    return arrayTypeAllocator.getId(allocator.getJavaType().getTypeName());
                } else {
                    return classTypeAllocator.getId(allocator.getJavaType().getTypeName());
                }
            }
        }
        throw new InternalException("Can not found typeId for id: " + id);
    }

    private boolean isMetaArray(Type javaType) {
        return ReadonlyArray.class.isAssignableFrom(ReflectUtils.getRawClass(javaType));
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
        String fileName = store.getFileName(javaType.getTypeName());
        if (store.fileNameExists(fileName)) {
            return new StdAllocator(store, fileName);
        } else {
            return new StdAllocator(
                    store,
                    fileName,
                    javaType,
                    allocateNextBaseId(javaType)
            );
        }
    }

    private long allocateNextBaseId(Type javaType) {
        long basedId;
        if (isChildArrayType(javaType)) {
            basedId = nextChildArrayBaseId;
            nextChildArrayBaseId += NUM_IDS_PER_ALLOCATOR;
        } else if (isReadWriteArray(javaType)) {
            basedId = nextReadWriteArrayBaseId;
            nextReadWriteArrayBaseId += NUM_IDS_PER_ALLOCATOR;
        } else if (isReadOnlyArray(javaType)) {
            basedId = nextReadOnlyArrayBaseId;
            nextReadOnlyArrayBaseId += NUM_IDS_PER_ALLOCATOR;
        } else {
            basedId = nextBaseId;
            nextBaseId += NUM_IDS_PER_ALLOCATOR;
        }
        return basedId;
    }

    private boolean isChildArrayType(Type javaType) {
        return ChildArray.class.isAssignableFrom(ReflectUtils.getRawClass(javaType));
    }

    private boolean isReadWriteArray(Type javaType) {
        return ReadWriteArray.class.isAssignableFrom(ReflectUtils.getRawClass(javaType));
    }

    private boolean isReadOnlyArray(Type javaType) {
        return ReadonlyArray.class.isAssignableFrom(ReflectUtils.getRawClass(javaType));
    }

    private String getTypeCode(java.lang.reflect.Type type) {
        if (type instanceof Class<?> klass) {
            return klass.getName();
        }
        if (type instanceof ParameterizedType pType) {
            return getTypeCode(pType.getRawType()) + "<" +
                    NncUtils.join(pType.getActualTypeArguments(), this::getTypeCode) + ">";
        }
        if (type instanceof WildcardType wildcardType) {
            if (ReflectUtils.isAllWildCardType(wildcardType)) {
                return "?";
            }
        }
        throw new InternalException("Can not get code for type: " + type);
    }

    public void save() {
        allocatorMap.values().forEach(StdAllocator::save);
    }

}
