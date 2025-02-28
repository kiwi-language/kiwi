package org.metavm.object.type;

import org.metavm.object.instance.core.*;

import java.util.ArrayList;
import java.util.List;

public class Indexes {

    public static List<Value> getIndexValues(IndexRef indexRef, Value key) {
        if (key instanceof StringReference) return List.of(key);
        if (key instanceof ValueReference vr && vr.get() instanceof ClassInstance obj) {
            var values = new ArrayList<Value>();
            obj.forEachField((f, v) -> values.add(v));
            return values;
        }
        var type = indexRef.getType();
        if (type instanceof PrimitiveType)
            return List.of(type.fromStackValue(key));
        else
            return List.of(key);
    }

}
