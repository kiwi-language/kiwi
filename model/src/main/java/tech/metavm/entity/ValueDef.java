package tech.metavm.entity;

import tech.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class ValueDef<T> extends PojoDef<T> {

    public ValueDef(Class<T> entityType,
                    Type genericType,
                    @Nullable PojoDef<? super T> parentDef,
                    Klass type,
                    DefContext defContext) {
        super(entityType, genericType, parentDef, type, defContext);
    }

}
