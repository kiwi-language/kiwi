package tech.metavm.transpile.ir;

import java.util.LinkedList;
import java.util.List;

public class ExtensionPath {

    private final LinkedList<IRType> types = new LinkedList<>();

    public void add(IRType type) {
        types.add(type);
    }

    public void removeLast() {
        types.removeLast();
    }

    public List<IRType> getTypes() {
        return types;
    }
}
