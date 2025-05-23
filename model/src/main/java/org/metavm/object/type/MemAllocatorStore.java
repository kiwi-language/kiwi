package org.metavm.object.type;

import org.metavm.util.InternalException;
import org.metavm.util.Utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class MemAllocatorStore implements AllocatorStore {


    public static final String DUMP_DIR = "/Users/leen/workspace/kiwi/test/src/test/resources/tmp";

    private long nextId = 10000L;
    private final List<String> fileNames = new ArrayList<>();
    private final Map<String, Properties> propertiesMap = new HashMap<>();

    @Override
    public String getFileName(String typeName) {
        return "/id/" + typeName + ".properties";
    }

    @Override
    public List<String> getFileNames() {
        return fileNames;
    }

    @Override
    public long getNextId() {
        return nextId;
    }

    @Override
    public void saveNextId(long nextId) {
        this.nextId = nextId;
    }

    @Override
    public Properties load(String fileName) {
        return Utils.safeCall(propertiesMap.get(fileName), this::copyProperties);
    }

    @Override
    public boolean fileNameExists(String fileName) {
        return propertiesMap.containsKey(fileName);
    }

    @Override
    public void saveFileNames(List<String> fileNames) {
        this.fileNames.clear();
        this.fileNames.addAll(fileNames);
        Collections.sort(this.fileNames);
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

    public void printAll() {
        propertiesMap.forEach((name, props) -> {
            try {
                props.store(System.out, "test");
            }
            catch (IOException ignored) {}
        });
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
