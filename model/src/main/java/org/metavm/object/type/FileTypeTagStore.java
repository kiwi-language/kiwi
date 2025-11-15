package org.metavm.object.type;

import org.metavm.util.InternalException;
import org.metavm.util.PropertiesUtils;

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
            properties.load(FileTypeTagStore.class.getResourceAsStream(FILE));
        } catch (IOException e) {
            throw new InternalException("Fail to load type tags file '" + FILE + "'", e);
        }
        nextTypeTag = Integer.parseInt(properties.getProperty(NEXT_TYPE_TAG, "4"));
        for (String propName : properties.stringPropertyNames()) {
            if (!propName.startsWith(SYSTEM_PROP_PREFIX))
                map.put(propName, Integer.parseInt(properties.getProperty(propName)));
        }
    }

    @Override
    public void save() {
        var properties = new Properties();
        properties.put(NEXT_TYPE_TAG, Integer.toString(nextTypeTag));
        map.forEach((name, tag) -> properties.put(name, tag.toString()));
        String filePath = cpRoot + FILE;
        try {
            PropertiesUtils.store(properties, new FileOutputStream(filePath));
        } catch (IOException e) {
            throw new InternalException("Fail to save type tags file '" + filePath + "'", e);
        }
    }
}
