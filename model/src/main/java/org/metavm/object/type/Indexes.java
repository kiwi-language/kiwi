package org.metavm.object.type;

import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;

import java.util.ArrayList;
import java.util.List;

public class Indexes {

    public static List<Value> getIndexValues(Value key) {
        if (key instanceof Reference ref) {
            if (ref.resolve() instanceof ClassInstance obj && obj.isValue()) {
                var values = new ArrayList<Value>();
                obj.forEachField((f, v) -> values.add(v));
                return values;
            }
        }
        return List.of(key);
    }

}
