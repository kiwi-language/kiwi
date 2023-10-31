package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

@Component
public class FileColumnStore extends MemColumnStore {

    private static final String FILE = "/column/columns.properties";

    private final String filePath;

    public FileColumnStore(@Value("${metavm.resource-cp-root}") String cpRoot) {
        this.filePath = cpRoot + FILE;
        var properties = new Properties();
        try {
            properties.load(new FileReader(filePath));
        } catch (IOException e) {
            throw new InternalException("Fail to load column file '" + filePath + "'", e);
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
        try {
            properties.store(new FileOutputStream(filePath), null);
        } catch (IOException e) {
            throw new InternalException("Fail to save column file '" + filePath + "'", e);
        }
    }

}
