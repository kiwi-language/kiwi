package tech.metavm.object.type;

import tech.metavm.util.NncUtils;

import java.io.IOException;
import java.util.*;

public class MemAllocatorStore implements AllocatorStore {

    private final Map<String, Properties> propertiesMap = new HashMap<>();

    @Override
    public String getFileName(String code) {
        return "/id/" + code + ".properties";
    }

    @Override
    public List<String> getFileNames() {
        return new ArrayList<>(propertiesMap.keySet());
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
}
