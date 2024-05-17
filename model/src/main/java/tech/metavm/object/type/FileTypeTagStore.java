package tech.metavm.object.type;

import tech.metavm.util.InternalException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class FileTypeTagStore extends MemTypeTagStore {

    public static final String SYSTEM_PROP_PREFIX = "$";

    public static final String NEXT_TYPE_TAG = "$nextTypeTag";

    private static final String FILE = "/typeTags/typeTags.properties";

    private final String cpRoot;

    public FileTypeTagStore(String cpRoot) {
        this.cpRoot = cpRoot;
        var properties = new Properties();
        try {
            properties.load(FileColumnStore.class.getResourceAsStream(FILE));
        } catch (IOException e) {
            throw new InternalException("Fail to load type tags file '" + FILE + "'", e);
        }
        if(properties.containsKey(NEXT_TYPE_TAG))
            nextTypeTag = Integer.parseInt(properties.getProperty(NEXT_TYPE_TAG));
        for (String propName : properties.stringPropertyNames()) {
            if(!propName.startsWith(SYSTEM_PROP_PREFIX))
                map.put(propName, Integer.parseInt(properties.getProperty(propName)));
        }
    }

    @Override
    public void save() {
        var properties = new Properties();
        properties.put(NEXT_TYPE_TAG, nextTypeTag);
        properties.putAll(map);
        String filePath = cpRoot + FILE;
        try {
            properties.store(new FileOutputStream(filePath), null);
        } catch (IOException e) {
            throw new InternalException("Fail to save type tags file '" + filePath + "'", e);
        }
    }
}
