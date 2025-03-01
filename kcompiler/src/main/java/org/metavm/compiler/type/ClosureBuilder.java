package org.metavm.compiler.type;

import org.metavm.compiler.util.List;

public class ClosureBuilder {
    private List<ClassType> types;

    private void union(Closure closure) {

    }


    private List<ClassType> intersection(List<ClassType> types1, List<ClassType> types2) {
        if (types1.isEmpty() || types2.isEmpty())
            return List.nil();
        var r = types1.head().compareTo(types2.head());
        if (r < 0)
            return intersection(types1.tail(), types2);
        else if (r == 0)
            return intersection(types1.tail(), types2.tail()).prepend(types1.head());
        else
            return intersection(types1, types2.tail());
    }

    private List<ClassType> union(List<ClassType> types1, List<ClassType> types2) {
        if (types1.isEmpty())
            return types2;
        if (types2.isEmpty())
            return types1;
        var r = types1.head().compareTo(types2.head());
        if (r < 0)
            return union(types1.tail(), types2).prepend(types1.head());
        else if (r == 0)
            return union(types1.tail(), types2.tail()).prepend(types1.head());
        else
            return union(types1, types2.tail()).prepend(types2.head());
    }

    private List<ClassType> insert(List<ClassType> types, ClassType type) {
        if (types.isEmpty())
            return List.of(type);
        var r = type.compareTo(types.head());
        if (r < 0)
            return types.prepend(type);
        else if (r == 0)
            return types;
        else
            return insert(types.tail(), type).prepend(types.head());
    }


}
