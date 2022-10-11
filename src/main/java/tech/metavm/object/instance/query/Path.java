package tech.metavm.object.instance.query;

import tech.metavm.util.NncUtils;

import java.util.*;

public class Path {

    public static Path merge(Path path1, Path...paths) {
        Path merged = path1.copy();
        for (Path p : paths) {
            merged.merge(p);
        }
        return merged;
    }

    public static Path merge(List<Path> paths) {
        if(NncUtils.isEmpty(paths)) {
            return Path.createRoot();
        }
        Path merged = Path.createRoot();
        for (Path path : paths) {
            merged.merge(path);
        }
        return merged;
    }

    public static Path createRoot() {
        return new Path("root");
    }

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

    public void addPath(List<String> strPath) {
        if(NncUtils.isEmpty(strPath)) {
            return;
        }
        Path child = getOrCreateChild(strPath.get(0));
        if(strPath.size() > 1) {
            child.addPath(strPath.subList(1, strPath.size()));
        }
    }

    public Path getOrCreateChild(String name) {
        return childMap.computeIfAbsent(name, Path::new);
    }

    public void fillPath(String pathStr) {
        int idx = pathStr.indexOf('.');
        if(idx > 0) {
            String childName = pathStr.substring(0, idx);
            getOrCreateChild(childName).fillPath(pathStr.substring(idx + 1));
        }
        else {
            getOrCreateChild(pathStr);
        }
    }

    public Path copy() {
        Path copy = new Path(name);
        childMap.forEach((childName, child) -> copy.childMap.put(childName, child.copy()));
        return copy;
    }

    public void merge(Path that) {
        that.childMap.forEach((childName, thatChild) -> {
            Path child = childMap.get(childName);
            if(child != null) {
                child.merge(thatChild);
            }
            else {
                childMap.put(childName, thatChild.copy());
            }
        });
    }

}
