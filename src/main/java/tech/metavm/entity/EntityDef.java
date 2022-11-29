package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;

import javax.annotation.Nullable;

public class EntityDef<T extends Entity> extends PojoDef<T> {

    public EntityDef(String name,
                     TypeReference<T> typeReference,
                     @Nullable PojoDef<? super T> parentDef,
                     Type type,
                     DefMap defMap
    ) {
        this(name, typeReference.getType(), parentDef, type, defMap);
    }

    public EntityDef(String name,
                     Class<T> entityType,
                     @Nullable PojoDef<? super T> parentDef,
                     Type type,
                     DefMap defMap
    ) {
        super(name, entityType, parentDef, type, defMap);
    }

    @Override
    protected void afterPojoCreated(T pojo, IInstance instance) {
        if(instance.getId() != null) {
            pojo.initId(instance.getId());
        }
    }

    @Override
    protected Long getId(T model) {
        return model.getId();
    }
}
