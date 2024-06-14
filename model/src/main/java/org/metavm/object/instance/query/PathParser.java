package org.metavm.object.instance.query;

import java.util.List;

public class PathParser {

    public static PathTree parse(List<String> paths) {
        PathTree root = new PathTree("root");
        paths.forEach(path -> addPath(root, path));
        return root;
    }

    private static void addPath(PathTree root, String path) {
        String[] splits = path.split("\\.");
        PathTree current = root;
        for (String split : splits) {
            current = current.getOrCreateChild(split);
        }
    }

}
