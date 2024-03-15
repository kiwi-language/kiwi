package tech.metavm.object.type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.ChildArray;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.entity.ReadonlyArray;
import tech.metavm.object.instance.core.TypeId;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.util.ReflectionUtils.getFieldQualifiedName;

public class StdAllocators {

    private static final Logger LOGGER = LoggerFactory.getLogger(StdAllocators.class);

    private static final long NUM_IDS_PER_ALLOCATOR = 10000L;

    private final Map<Type, StdAllocator> allocatorMap = new HashMap<>();
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
        return switch (object) {
            case java.lang.reflect.Field field -> getId0(Field.class, getFieldQualifiedName(field));
            case Type type -> getId0(ClassType.class, getTypeCode(type));
            case Enum<?> enumConstant -> getId0(ReflectionUtils.getEnumClass(enumConstant), enumConstant.name());
            case ModelIdentity modelIdentity -> getId0(modelIdentity.type(), modelIdentity.name());
            case null, default ->
                    throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        };
    }

    public void putId(Object object, long id) {
        switch (object) {
            case java.lang.reflect.Field field -> putId0(Field.class, getFieldQualifiedName(field), id);
            case Type type -> putId0(ClassType.class, getTypeCode(type), id);
            case Enum<?> enumConstant -> putId0(ReflectionUtils.getEnumClass(enumConstant), enumConstant.name(), id);
            case ModelIdentity modelIdentity -> putId0(modelIdentity.type(), modelIdentity.name(), id);
            case null, default ->
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

    public TypeId getTypeId(long id) {
        StdAllocator classTypeAllocator = allocatorMap.get(ClassType.class);
        StdAllocator arrayTypeAllocator = allocatorMap.get(ArrayType.class);
        for (StdAllocator allocator : allocatorMap.values()) {
            if (allocator.contains(id)) {
                if (isMetaArray(allocator.getJavaType())) {
                    return TypeId.ofArray(arrayTypeAllocator.getId(allocator.getJavaType().getTypeName()));
                } else {
                    return TypeId.ofClass(classTypeAllocator.getId(allocator.getJavaType().getTypeName()));
                }
            }
        }
        throw new InternalException("Can not found typeId for id: " + id);
    }

    private boolean isMetaArray(Type javaType) {
        return ReadonlyArray.class.isAssignableFrom(ReflectionUtils.getRawClass(javaType));
    }

    public Map<Type, List<Long>> allocate(Map<? extends Type, Integer> typeId2count) {
        Map<Type, List<Long>> result = new HashMap<>();
        typeId2count.forEach((javaType, count) -> {
//            Class<?> javaType = ModelDefRegistry.getJavaType(type);
            List<Long> ids = getAllocator(javaType).allocate(count);
            result.put(javaType, ids);
        });
        return result;
    }

    private StdAllocator getAllocator(Type javaType) {
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
        return ChildArray.class.isAssignableFrom(ReflectionUtils.getRawClass(javaType));
    }

    private boolean isReadWriteArray(Type javaType) {
        return ReadWriteArray.class.isAssignableFrom(ReflectionUtils.getRawClass(javaType));
    }

    private boolean isReadOnlyArray(Type javaType) {
        return ReadonlyArray.class.isAssignableFrom(ReflectionUtils.getRawClass(javaType));
    }

    private String getTypeCode(Type type) {
        if (type instanceof Class<?> klass) {
            return klass.getName();
        }
        if (type instanceof ParameterizedType pType) {
            return getTypeCode(pType.getRawType()) + "<" +
                    NncUtils.join(pType.getActualTypeArguments(), this::getTypeCode) + ">";
        }
        if (type instanceof WildcardType wildcardType) {
            if (ReflectionUtils.isAllWildCardType(wildcardType)) {
                return "?";
            }
        }
        throw new InternalException("Can not get code for type: " + type);
    }

    public void save() {
        store.saveFileNames(NncUtils.map(allocatorMap.values(), StdAllocator::getFileName));
        allocatorMap.values().forEach(StdAllocator::save);
    }

    public Map<String, Long> getIdMap() {
        var ids = new HashMap<String, Long>();
        allocatorMap.values().forEach(a -> a.buildIdMap(ids));
        return ids;
    }

}
