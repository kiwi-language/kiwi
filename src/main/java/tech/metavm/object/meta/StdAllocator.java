package tech.metavm.object.meta;

import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

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
//    private final long base;
    private long nextId;
    private final Map<String, Long> code2id = new LinkedHashMap<>();
    private final Map<Long, String> id2code = new LinkedHashMap<>();
    private final Class<?> javaType;

    public StdAllocator(AllocatorStore store, String fileName, Class<?> javaType, long base) {
        this.store = store;
        this.fileName = fileName;
        this.javaType = javaType;
        nextId = base;
    }

    public StdAllocator(AllocatorStore store, String fileName) {
        this.store = store;
        Properties properties = store.load(fileName);
        nextId = Long.parseLong(properties.getProperty(ID_BASE_PROP_KEY));
        javaType = ReflectUtils.classForName(properties.getProperty(TYPE_CODE_PROP_KEY));
        this.fileName = fileName;
        for (String propertyName : properties.stringPropertyNames()) {
            if(!propertyName.startsWith(SYSTEM_PROP_PREFIX)) {
                long id = Long.parseLong(properties.getProperty(propertyName));
                putId(propertyName, id);
                if(id >= nextId) {
                    nextId = id + 1;
                }
            }
        }
    }

    public Long getId(String code) {
        return code2id.get(code);
//        if(id == null) {
//            id = allocateId(code);
//        }
//        return id;
    }

    public String getCodeById(long id) {
        for (Map.Entry<String, Long> entry : code2id.entrySet()) {
            if(entry.getValue() == id) {
                return entry.getKey();
            }
        }
        throw new InternalException("code not found for id: " + id);
    }

    private long allocateId(String code) {
        long id = nextId++;
        putId(code, id);
        return id;
    }

    public boolean contains(long id) {
        return id2code.containsKey(id);
    }

    public long getNextId() {
        return nextId;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public String getFileName() {
        return fileName;
    }

    public void save() {
        Properties properties = new Properties();
        properties.put(ID_BASE_PROP_KEY, Long.toString(nextId));
        properties.put(TYPE_CODE_PROP_KEY, javaType.getName());
        code2id.forEach((code, id) -> properties.put(code, id.toString()));
        store.save(fileName, properties);
    }

    public List<Long> allocate(Integer count) {
        List<Long> result = NncUtils.range(nextId, nextId + count);
        nextId += count;
        return result;
    }

    public void putId(String code, long id) {
        code2id.put(code, id);
        id2code.put(id, code);
    }

    public boolean isArray() {
        return javaType == Table.class;
    }

}
