package tech.metavm.object.instance.query;

import java.util.*;

public class Path {
    private final Map<String, Path> childMap = new LinkedHashMap<>();
    private final String name;

    public Path(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<Path> getChildren() {
        return childMap.values();
    }

    public Path getOrCreate(String name) {
        return childMap.computeIfAbsent(name, Path::new);
    }

    public void fillPath(String pathStr) {
        int idx = pathStr.indexOf('.');
        if(idx > 0) {
            String childName = pathStr.substring(0, idx);
            getOrCreate(childName).fillPath(pathStr.substring(idx + 1));
        }
        else {
            getOrCreate(pathStr);
        }
    }

}
