package org.metavm.object.type;

import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.Instances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Klasses {

    public static void loadKlasses(IInstanceContext context) {
        var klasses = context.selectByKey(Klass.IDX_ALL_FLAG, Instances.trueInstance());
        var sortedKlasses = new ArrayList<Klass>();
        var visited = new HashSet<Klass>();
        for (Klass klass : klasses) Klasses.sortKlass(klass, sortedKlasses, visited);
        for (var klass : sortedKlasses) klass.resetHierarchy();

    }

    private static void sortKlass(Klass klass, List<Klass> result, Set<Klass> visited) {
        if (!visited.add(klass)) return;
        var superKlass = klass.getSuperKlass();
        if (superKlass != null) sortKlass(superKlass, result, visited);
        result.add(klass);
    }

}
