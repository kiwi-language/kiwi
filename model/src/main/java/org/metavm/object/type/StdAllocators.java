package org.metavm.object.type;

import org.metavm.entity.ChildArray;
import org.metavm.entity.ModelIdentity;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.ReadonlyArray;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TypeId;
import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.metavm.util.ReflectionUtils.getFieldQualifiedName;

public class StdAllocators {

    private static final Logger logger = LoggerFactory.getLogger(StdAllocators.class);

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

    public Id getId(Object object) {
        return switch (object) {
            case java.lang.reflect.Field field -> getId0(Field.class, getFieldQualifiedName(field));
            case Type type -> getId0(Klass.class, getTypeCode(type));
            case Enum<?> enumConstant -> getId0(ReflectionUtils.getEnumClass(enumConstant), enumConstant.name());
            case ModelIdentity modelIdentity -> getId0(modelIdentity.type(), modelIdentity.name());
            case null, default ->
                    throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        };
    }

    public void putId(Object object, Id id) {
        putId(object, id, null);
    }

    public void putId(Object object, Id id, @Nullable Long nextNodeId) {
        switch (object) {
            case java.lang.reflect.Field field -> putId0(Field.class, getFieldQualifiedName(field), id, nextNodeId);
            case Type type -> putId0(Klass.class, getTypeCode(type), id, nextNodeId);
            case Enum<?> enumConstant -> putId0(ReflectionUtils.getEnumClass(enumConstant), enumConstant.name(), id, nextNodeId);
            case ModelIdentity modelIdentity -> putId0(modelIdentity.type(), modelIdentity.name(), id, nextNodeId);
            case null, default ->
                    throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        }
    }

    public StdAllocator getAllocatorById(Id id) {
        return NncUtils.find(allocatorMap.values(), a -> a.contains(id));
    }

    private Id getId0(Type javaType, String entityCode) {
        return getAllocator(javaType).getId(entityCode);
    }

    private void putId0(Type javaType, String entityCode, Id id, @Nullable Long nextNodeId) {
        allocatorMap.get(javaType).putId(entityCode, id, nextNodeId);
    }

    public TypeId getTypeId(Id id) {
        StdAllocator classTypeAllocator = allocatorMap.get(Klass.class);
        StdAllocator arrayTypeAllocator = allocatorMap.get(ArrayType.class);
        for (StdAllocator allocator : allocatorMap.values()) {
            if (allocator.contains(id)) {
                if (isMetaArray(allocator.getJavaType())) {
                    return TypeId.ofArray(arrayTypeAllocator.getId(allocator.getJavaType().getTypeName()).getTreeId());
                } else {
                    return TypeId.ofClass(classTypeAllocator.getId(allocator.getJavaType().getTypeName()).getTreeId());
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
            var ids = getAllocator(javaType).allocate(count);
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

    public Map<String, Id> getIdMap() {
        var ids = new HashMap<String, Id>();
        allocatorMap.values().forEach(a -> a.buildIdMap(ids));
        return ids;
    }

    public @Nullable Long getNextNodeId(Object entity) {
        if(entity instanceof ModelIdentity modelIdentity)
            return getAllocator(modelIdentity.type()).getNextNodeId(modelIdentity.name());
        else
            throw new IllegalArgumentException("Invalid entity: " + entity);
    }
}
