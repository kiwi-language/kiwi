package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.entity.ChildArray;
import org.metavm.entity.ReadWriteArray;
import org.metavm.entity.ReadonlyArray;
import org.metavm.object.instance.core.Id;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.TypeParser;

import java.lang.reflect.Type;
import java.util.*;

public class StdAllocator {

    public static final String SYSTEM_PROP_PREFIX = "$";
    public static final String ID_BASE_PROP_KEY = "$base";
    public static final String TYPE_CODE_PROP_KEY = "$typeCode";

    private final AllocatorStore store;
    private final String fileName;
    private long nextId;
    private final Map<String, Id> code2id = new LinkedHashMap<>();
    private final Map<Id, String> id2code = new LinkedHashMap<>();
    private final Map<String, Long> code2nextNodeId = new HashMap<>();
    private final Type javaType;

    public StdAllocator(AllocatorStore store, String fileName, Type javaType, long base) {
        this.store = store;
        this.fileName = fileName;
        this.javaType = javaType;
        nextId = base;
    }

    public StdAllocator(AllocatorStore store, String fileName) {
        this.store = store;
        Properties properties = store.load(fileName);
        nextId = Long.parseLong(properties.getProperty(ID_BASE_PROP_KEY));
        javaType = TypeParser.parse(properties.getProperty(TYPE_CODE_PROP_KEY));
        this.fileName = fileName;
        for (String code : properties.stringPropertyNames()) {
            if(!code.startsWith(SYSTEM_PROP_PREFIX)) {
                var idStr = properties.getProperty(code);
                var idx = idStr.indexOf(':');
                Id id;
                Long nextNodeId;
                if(idx == -1) {
                    id = Id.parse(idStr);
                    nextNodeId = null;
                }
                else {
                    id = Id.parse(idStr.substring(0, idx));
                    nextNodeId = Long.parseLong(idStr.substring(idx + 1));
                }
                putId(code, id, nextNodeId);
                var physicalId = id.getTreeId();
                if(physicalId >= nextId)
                    nextId = physicalId + 1;
            }
        }
    }

    public Id getId(String code) {
        return code2id.get(code);
    }

    public @Nullable Long getNextNodeId(String code) {
        return code2nextNodeId.get(code);
    }

    public void buildIdMap(Map<String, Id> ids) {
        code2id.forEach((code, id) -> ids.put(javaType.getTypeName() + "." + code, id));
    }

    public boolean contains(Id id) {
        return id2code.containsKey(id);
    }

    public long getNextId() {
        return nextId;
    }

    public Type getJavaType() {
        return javaType;
    }

    public void save() {
        Properties properties = new Properties();
        properties.put(ID_BASE_PROP_KEY, Long.toString(nextId));
        properties.put(TYPE_CODE_PROP_KEY, javaType.getTypeName());
        code2id.forEach((code, id) -> {
            var nextNodeId = code2nextNodeId.get(code);
            var idStr = nextNodeId == null ? id.toString() : id.toString() + ":" + nextNodeId;
            properties.put(code, idStr);
        });
        store.save(fileName, properties);
    }

    public List<Long> allocate(Integer count) {
        List<Long> result = NncUtils.range(nextId, nextId + count);
        nextId += count;
        return result;
    }

    public String getFileName() {
        return fileName;
    }

    public void putId(@NotNull String code, @NotNull Id id, @Nullable Long nextNodeId) {
        code2id.put(code, id);
        id2code.put(id, code);
        if(nextNodeId != null)
            code2nextNodeId.put(code, nextNodeId);
    }

    public boolean isReadWriteArray() {
        return ReflectionUtils.getRawClass(javaType) == ReadWriteArray.class;
    }

    public boolean isChildArray() {
        return ReflectionUtils.getRawClass(javaType) == ChildArray.class;
    }

    public boolean isReadonlyArray() {
        return ReflectionUtils.getRawClass(javaType) == ReadonlyArray.class;
    }

}
