package org.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.object.instance.core.Id;
import org.metavm.util.TypeParser;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class StdAllocator {

    public static final String SYSTEM_PROP_PREFIX = "$";
    public static final String TYPE_CODE_PROP_KEY = "$typeCode";

    private final AllocatorStore store;
    private final String fileName;
    private final Map<String, Id> name2id = new LinkedHashMap<>();
    private final Map<Id, String> id2name = new LinkedHashMap<>();
    private final Map<String, Long> code2nextNodeId = new HashMap<>();
    private final Type javaType;

    public StdAllocator(AllocatorStore store, String fileName, Type javaType) {
        this.store = store;
        this.fileName = fileName;
        this.javaType = javaType;
    }

    public StdAllocator(AllocatorStore store, String fileName) {
        this.store = store;
        Properties properties = store.load(fileName);
        javaType = TypeParser.parse(properties.getProperty(TYPE_CODE_PROP_KEY));
        this.fileName = fileName;
        for (String name : properties.stringPropertyNames()) {
            if(!name.startsWith(SYSTEM_PROP_PREFIX)) {
                var idStr = properties.getProperty(name);
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
                putId(name, id, nextNodeId);
            }
        }
    }

    public Id getId(String name) {
        return name2id.get(name);
    }

    public @Nullable Long getNextNodeId(String name) {
        return code2nextNodeId.get(name);
    }

    public void buildIdMap(Map<String, Id> ids) {
        name2id.forEach((code, id) -> ids.put(javaType.getTypeName() + "." + code, id));
    }

    public boolean contains(Id id) {
        return id2name.containsKey(id);
    }

    public Type getJavaType() {
        return javaType;
    }

    public void save() {
        Properties properties = new Properties();
        properties.put(TYPE_CODE_PROP_KEY, javaType.getTypeName());
        name2id.forEach((code, id) -> {
            var nextNodeId = code2nextNodeId.get(code);
            var idStr = nextNodeId == null ? id.toString() : id.toString() + ":" + nextNodeId;
            properties.put(code, idStr);
        });
        store.save(fileName, properties);
    }

    public String getFileName() {
        return fileName;
    }

    public void putId(@NotNull String name, @NotNull Id id, @Nullable Long nextNodeId) {
        name2id.put(name, id);
        id2name.put(id, name);
        if(nextNodeId != null)
            code2nextNodeId.put(name, nextNodeId);
    }

}
