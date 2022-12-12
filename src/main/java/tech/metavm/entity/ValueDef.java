package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.meta.ClassType;
import tech.metavm.util.TypeReference;

import java.lang.reflect.Type;

public class ValueDef<T> extends PojoDef<T> {

    public ValueDef(TypeReference<T> typeReference,
                    @Nullable PojoDef<? super T> parentDef,
                    ClassType type,
                    DefMap defMap) {
        this(typeReference.getType(), typeReference.getGenericType(), parentDef, type, defMap);
    }

    public ValueDef(Class<T> entityType,
                    Type genericType,
                    @Nullable PojoDef<? super T> parentDef,
                    ClassType type,
                    DefMap defMap) {
        super(entityType, genericType, parentDef, type, defMap);
    }

}
