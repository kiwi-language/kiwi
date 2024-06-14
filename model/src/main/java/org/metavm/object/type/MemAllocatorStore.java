package org.metavm.object.type;

import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class MemAllocatorStore implements AllocatorStore {


    public static final String DUMP_DIR = "/Users/leen/workspace/object/test/src/test/resources/tmp";

    private final List<String> fileNames = new ArrayList<>();
    private final Map<String, Properties> propertiesMap = new HashMap<>();

    @Override
    public String getFileName(String code) {
        return "/id/" + code + ".properties";
    }

    @Override
    public List<String> getFileNames() {
        return fileNames;
    }

    @Override
    public Properties load(String fileName) {
        return NncUtils.get(propertiesMap.get(fileName), this::copyProperties);
    }

    @Override
    public boolean fileNameExists(String fileName) {
        return propertiesMap.containsKey(fileName);
    }

    @Override
    public void saveFileNames(List<String> fileNames) {
        this.fileNames.clear();
        this.fileNames.addAll(fileNames);
    }

    @Override
    public void save(String fileName, Properties properties) {
        propertiesMap.put(fileName, copyProperties(properties));
    }

    private Properties copyProperties(Properties properties) {
        return (Properties) properties.clone();
    }

    public void clear() {
        propertiesMap.clear();
    }

    public MemAllocatorStore copy() {
        var result = new MemAllocatorStore();
        result.fileNames.addAll(fileNames);
        for (var entry : propertiesMap.entrySet()) {
            result.propertiesMap.put(entry.getKey(), copyProperties(entry.getValue()));
        }
        return result;
    }

    public void print(String fileName) {
        var props = propertiesMap.get(fileName);
        try {
            props.store(System.out, "test");
        } catch (IOException ignored) {
        }
    }

    public void dump() {
        for (var entry : propertiesMap.entrySet()) {
            var fileName = entry.getKey();
            var props = entry.getValue();
            try (var output = new FileOutputStream(DUMP_DIR + fileName)) {
                props.store(output, "test");
            } catch (IOException e) {
                throw new InternalException(e);
            }
        }
    }
}
