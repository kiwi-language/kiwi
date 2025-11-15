package org.metavm.object.type;

import org.metavm.entity.ModelIdentity;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.TypeId;
import org.metavm.util.InternalException;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StdAllocators {

    private final Map<String, StdAllocator> allocatorMap = new HashMap<>();
    private long nextId;
    private final AllocatorStore store;

    public StdAllocators(AllocatorStore store) {
        this.store = store;
        nextId = store.getNextId();
        for (String fileName : store.getFileNames()) {
            StdAllocator allocator = new StdAllocator(store, fileName);
            allocatorMap.put(allocator.getJavaType(), allocator);
        }
    }

    public Id getId(Object object) {
        return switch (object) {
            case Type type -> getId0(Klass.class.getName(), getTypeCode(type));
            case ModelIdentity modelIdentity -> getId0(modelIdentity.type().getTypeName(), modelIdentity.name());
            case null, default ->
                    throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        };
    }

    public void putId(Object object, Id id) {
        putId(object, id, null);
    }

    public void putId(Object object, Id id, @Nullable Long nextNodeId) {
        switch (object) {
            case Type type -> putId0(Klass.class.getName(), getTypeCode(type), id, nextNodeId);
            case ModelIdentity modelIdentity -> putId0(modelIdentity.type().getTypeName(), modelIdentity.name(), id, nextNodeId);
            case null, default ->
                    throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        }
    }

    private Id getId0(String javaType, String entityCode) {
        return getAllocator(javaType).getId(entityCode);
    }

    private void putId0(String javaType, String entityCode, Id id, @Nullable Long nextNodeId) {
        getAllocator(javaType).putId(entityCode, id, nextNodeId);
    }

    public TypeId getTypeId(Id id) {
        StdAllocator classTypeAllocator = allocatorMap.get(Klass.class.getName());
        for (StdAllocator allocator : allocatorMap.values()) {
            if (allocator.contains(id))
                return TypeId.ofClass(classTypeAllocator.getId(allocator.getJavaType()).getTreeId());
        }
        throw new InternalException("Can not found typeId for id: " + id);
    }

    public List<Long> allocate(int count) {
        var ids = new ArrayList<Long>();
        for (var i = 0; i < count; i++) {
            ids.add(nextId());
        }
        return ids;
    }

    private long nextId() {
        return nextId++;
    }

    private StdAllocator getAllocator(String javaType) {
        return allocatorMap.computeIfAbsent(javaType, this::createAllocator);
    }

    private StdAllocator createAllocator(String javaType) {
        String fileName = store.getFileName(javaType);
        if (store.fileNameExists(fileName)) {
            return new StdAllocator(store, fileName);
        } else {
            return new StdAllocator(store, fileName, javaType);
        }
    }

    private String getTypeCode(Type type) {
        if (type instanceof Class<?> klass) {
            return klass.getName();
        }
        if (type instanceof ParameterizedType pType) {
            return getTypeCode(pType.getRawType()) + "<" +
                    Utils.join(pType.getActualTypeArguments(), this::getTypeCode) + ">";
        }
        throw new InternalException("Can not get code for type: " + type);
    }

    public void save() {
        store.saveNextId(nextId);
        store.saveFileNames(Utils.map(allocatorMap.values(), StdAllocator::getFileName));
        allocatorMap.values().forEach(StdAllocator::save);
    }

    public Map<String, Id> getIdMap() {
        var ids = new HashMap<String, Id>();
        allocatorMap.values().forEach(a -> a.buildIdMap(ids));
        return ids;
    }

    public @Nullable Long getNextNodeId(Object entity) {
        if(entity instanceof ModelIdentity modelIdentity)
            return getAllocator(modelIdentity.type().getTypeName()).getNextNodeId(modelIdentity.name());
        else
            throw new IllegalArgumentException("Invalid entity: " + entity);
    }
}
