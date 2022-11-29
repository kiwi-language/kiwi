package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;

public class ValueDef<T> extends PojoDef<T> {

    public ValueDef(String name,
                    TypeReference<T> typeReference,
                    @Nullable PojoDef<? super T> parentDef,
                    Type type,
                    DefMap defMap) {
        this(name, typeReference.getType(), parentDef, type, defMap);
    }

    public ValueDef(String name,
                    Class<T> entityType,
                    @Nullable PojoDef<? super T> parentDef,
                    Type type,
                    DefMap defMap) {
        super(name, entityType, parentDef, type, defMap);
    }

}
