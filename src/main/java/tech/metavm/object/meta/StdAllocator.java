package tech.metavm.object.meta;

import tech.metavm.util.InternalException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public class StdAllocator {

    public static final String ID_BASE_PROP_KEY = "$base";

    private final String fileName;
    private final long base;
    private long nextId;
    private final Map<String, Long> code2id = new LinkedHashMap<>();

    public StdAllocator(String fileName) {
        Properties properties = new Properties();
        try {
            properties.load(StdAllocator.class.getResourceAsStream(fileName));
        }
        catch (IOException e) {
            throw new InternalException("fail to load id properties file: " + fileName);
        }
        base = nextId= Long.parseLong(properties.getProperty(ID_BASE_PROP_KEY));
        this.fileName = fileName;
        for (String propertyName : properties.stringPropertyNames()) {
            if(!propertyName.equals(ID_BASE_PROP_KEY)) {
                long id = Long.parseLong(properties.getProperty(propertyName));
                code2id.put(propertyName, id);
                if(id >= nextId) {
                    nextId = id + 1;
                }
            }
        }
    }

    public long getId(String code) {
        return code2id.computeIfAbsent(code, this::allocateId);
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
        code2id.put(code, id);
        return id;
    }

    public void save() {
        Properties properties = new Properties();
        properties.put(ID_BASE_PROP_KEY, base);
        code2id.forEach((code, id) -> properties.put(code, id.toString()));
        try {
            properties.store(new FileOutputStream(fileName), null);
        }
        catch (IOException e) {
            throw new InternalException("Fail to save id properties to file: "+ fileName);
        }
    }

}
