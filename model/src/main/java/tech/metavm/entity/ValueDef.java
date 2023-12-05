package tech.metavm.entity;

import org.jetbrains.annotations.Nullable;
import tech.metavm.object.type.ClassType;

import java.lang.reflect.Type;

public class ValueDef<T> extends PojoDef<T> {

    public ValueDef(Class<T> entityType,
                    Type genericType,
                    @Nullable PojoDef<? super T> parentDef,
                    ClassType type,
                    DefContext defContext) {
        super(entityType, genericType, parentDef, type, defContext);
    }

}
