package org.metavm.object.instance.query;

import org.metavm.util.Utils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class PathTree {

    public static PathTree create(String name, List<PathTree> children) {
        PathTree path = new PathTree(name);
        for (PathTree child : children) {
            path.childMap.put(child.getName(), child);
        }
        return path;
    }

    public static PathTree merge(PathTree path1, PathTree...paths) {
        PathTree merged = path1.copy();
        for (PathTree p : paths) {
            merged.merge(p);
        }
        return merged;
    }

    public static PathTree merge(List<PathTree> paths) {
        if(Utils.isEmpty(paths)) {
            return PathTree.createRoot();
        }
        PathTree merged = PathTree.createRoot();
        for (PathTree path : paths) {
            merged.merge(path);
        }
        return merged;
    }

    public static PathTree createRoot() {
        return new PathTree("root");
    }

    private final Map<String, PathTree> childMap = new LinkedHashMap<>();
    private final String name;

    public PathTree(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Collection<PathTree> getChildren() {
        return childMap.values();
    }

    public void addPath(Path path) {
        if(path.isEmpty()) {
            return;
        }
        PathTree child = getOrCreateChild(path.firstItem());
        if(path.hasSubPath()) {
            child.addPath(path.subPath());
        }
    }

    public PathTree changeName(String name) {
        PathTree path = new PathTree(name);
        path.childMap.putAll(this.childMap);
        return path;
    }

    public PathTree getOrCreateChild(String name) {
        return childMap.computeIfAbsent(name, PathTree::new);
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

    public PathTree copy() {
        PathTree copy = new PathTree(name);
        childMap.forEach((childName, child) -> copy.childMap.put(childName, child.copy()));
        return copy;
    }

    public void merge(PathTree that) {
        that.childMap.forEach((childName, thatChild) -> {
            PathTree child = childMap.get(childName);
            if(child != null) {
                child.merge(thatChild);
            }
            else {
                childMap.put(childName, thatChild.copy());
            }
        });
    }

}
