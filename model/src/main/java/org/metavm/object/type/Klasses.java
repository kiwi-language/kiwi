package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class Klasses {

    public static List<Klass> getToplevelKlasses(IInstanceContext context) {
        var klasses = context.selectByKey(Klass.IDX_ALL_FLAG, Instances.trueInstance());
        return Utils.filter(klasses, k -> k.getScope() == null);
    }

    public static List<Klass> loadKlasses(IInstanceContext context) {
        var klasses = context.selectByKey(Klass.IDX_ALL_FLAG, Instances.trueInstance());
        var sortedKlasses = new ArrayList<Klass>();
        var visited = new HashSet<Klass>();
        for (Klass klass : klasses) Klasses.sortKlass(klass, sortedKlasses, visited);
        for (var klass : sortedKlasses) klass.resetHierarchy();
        return klasses;
    }

    private static void sortKlass(Klass klass, List<Klass> result, Set<Klass> visited) {
        if (!visited.add(klass)) return;
        var superKlass = klass.getSuperKlass();
        if (superKlass != null) sortKlass(superKlass, result, visited);
        result.add(klass);
    }

}
