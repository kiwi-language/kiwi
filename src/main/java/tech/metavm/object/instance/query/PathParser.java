package tech.metavm.object.instance.query;

import java.util.List;

public class PathParser {

    public static Path parse(List<String> paths) {
        Path root = new Path("root");
        paths.forEach(path -> addPath(root, path));
        return root;
    }

    private static void addPath(Path root, String path) {
        String[] splits = path.split("\\.");
        Path current = root;
        for (String split : splits) {
            current = current.getOrCreate(split);
        }
    }

}
