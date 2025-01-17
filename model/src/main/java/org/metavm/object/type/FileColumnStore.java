package org.metavm.object.type;

import org.metavm.util.InternalException;
import org.metavm.util.Utils;
import org.metavm.util.PropertiesUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;
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
            Utils.require(idx > 0, "Invalid column file property name '" + propName + "'");
            String typeName = propName.substring(0, idx);
            String fieldName = propName.substring(idx + 1);
            var splits = properties.getProperty(propName).split(",");
            columnNameMap.computeIfAbsent(typeName, k -> new HashMap<>()).put(fieldName, splits[0]);
            tagMap.computeIfAbsent(typeName, k -> new HashMap<>()).put(fieldName, Integer.valueOf(splits[1]));
        }
    }

    @Override
    public void save() {
        var properties = new Properties();
        for (var entry : columnNameMap.entrySet()) {
            for (var subEntry : entry.getValue().entrySet()) {
                var tag = Objects.requireNonNull(tagMap.get(entry.getKey()).get(subEntry.getKey()),
                        "Tag not found for " + entry.getKey() + "." + subEntry.getKey());
                properties.put(entry.getKey() + "." + subEntry.getKey(), subEntry.getValue() + "," + tag);
            }
        }
        String filePath = cpRoot + FILE;
        try {
            PropertiesUtils.store(properties, new FileOutputStream(filePath));
        } catch (IOException e) {
            throw new InternalException("Fail to save column file '" + filePath + "'", e);
        }
    }

}
