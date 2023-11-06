package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;

import java.lang.reflect.Type;
import java.util.List;

public interface DefParser<T,I extends Instance,D extends ModelDef<T,I>> {

    D create();

    void initialize();

    List<Type> getDependencyTypes();

    default ModelDef<?,?> createAndInitialize() {
        D def = create();
        initialize();
        return def;
    }
}
