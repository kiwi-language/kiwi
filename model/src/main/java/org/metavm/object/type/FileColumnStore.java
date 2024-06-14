package org.metavm.object.type;

import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class FileColumnStore extends MemColumnStore {

    private static final String FILE = "/column/columns.properties";

    private final String cpRoot;

    public FileColumnStore(String cpRoot) {
        this.cpRoot = cpRoot;
        var properties = new Properties();
        try {
            properties.load(FileColumnStore.class.getResourceAsStream(FILE));
        } catch (IOException e) {
            throw new InternalException("Fail to load column file '" + FILE + "'", e);
        }
        for (String propName : properties.stringPropertyNames()) {
            int idx = propName.lastIndexOf('.');
            NncUtils.requireNonNull(idx > 0, "Invalid column file property name '" + propName + "'");
            String typeName = propName.substring(0, idx);
            String fieldName = propName.substring(idx + 1);
            columnNameMap.computeIfAbsent(typeName, k -> new HashMap<>())
                    .put(fieldName, properties.getProperty(propName));
        }
    }

    @Override
    public void save() {
        var properties = new Properties();
        for (var entry : columnNameMap.entrySet()) {
            for (var subEntry : entry.getValue().entrySet()) {
                properties.put(entry.getKey() + "." + subEntry.getKey(), subEntry.getValue());
            }
        }
        String filePath = cpRoot + FILE;
        try {
            properties.store(new FileOutputStream(filePath), null);
        } catch (IOException e) {
            throw new InternalException("Fail to save column file '" + filePath + "'", e);
        }
    }

}
