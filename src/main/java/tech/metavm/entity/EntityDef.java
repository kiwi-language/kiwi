package tech.metavm.entity;

import tech.metavm.object.meta.ClassType;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class EntityDef<T extends Entity> extends PojoDef<T> {

    public EntityDef(TypeReference<T> typeReference,
                     @Nullable PojoDef<? super T> parentDef,
                     ClassType type,
                     DefMap defMap
    ) {
        this(typeReference.getType(), typeReference.getGenericType() , parentDef, type, defMap);
    }

    public EntityDef(Class<T> javaClass,
                     Type javaType,
                     @Nullable PojoDef<? super T> superDef,
                     ClassType type,
                     DefMap defMap
    ) {
        super(javaClass, javaType, superDef, type, defMap);
    }

    @Override
    protected Long getId(T model) {
        return model.getId();
    }
}
