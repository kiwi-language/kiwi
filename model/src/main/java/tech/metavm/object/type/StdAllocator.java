package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.ChildArray;
import tech.metavm.entity.ReadWriteArray;
import tech.metavm.entity.ReadonlyArray;
import tech.metavm.object.instance.core.Id;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;
import tech.metavm.util.TypeParser;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class StdAllocator {

    public static final String SYSTEM_PROP_PREFIX = "$";
    public static final String ID_BASE_PROP_KEY = "$base";
    public static final String TYPE_CODE_PROP_KEY = "$typeCode";

    private final AllocatorStore store;
    private final String fileName;
    private long nextId;
    private final Map<String, Id> code2id = new LinkedHashMap<>();
    private final Map<Id, String> id2code = new LinkedHashMap<>();
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
        for (String propertyName : properties.stringPropertyNames()) {
            if(!propertyName.startsWith(SYSTEM_PROP_PREFIX)) {
                var id = Id.parse(properties.getProperty(propertyName));
                putId(propertyName, id);
                var physicalId = id.getPhysicalId();
                if(physicalId >= nextId) {
                    nextId = physicalId + 1;
                }
            }
        }
    }

    public Id getId(String code) {
        return code2id.get(code);
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
        code2id.forEach((code, id) -> properties.put(code, id.toString()));
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

    public void putId(@NotNull String code, @NotNull Id id) {
        code2id.put(code, id);
        id2code.put(id, code);
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
