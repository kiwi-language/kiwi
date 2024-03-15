package tech.metavm.entity;

import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.ClassType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class EntityDef<T extends Entity> extends PojoDef<T> {

    public EntityDef(Class<T> javaClass,
                     Type javaType,
                     @Nullable PojoDef<? super T> superDef,
                     ClassType type,
                     DefContext defContext
    ) {
        super(javaClass, javaType, superDef, type, defContext);
    }

    @Override
    protected Id getId(T model) {
        return model.tryGetId();
    }
}
