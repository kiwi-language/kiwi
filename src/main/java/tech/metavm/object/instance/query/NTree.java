package tech.metavm.object.instance.query;

import java.util.List;

public abstract class NTree {

    protected final Path path;

    protected NTree(Path path) {
        this.path = path;
    }

    public final String getName() {
        return path.getName();
    }

    public final Path getPath() {
        return path;
    }

    public abstract List<ObjectTree> getChildObjectTrees();

    public abstract Object getValue();

}
