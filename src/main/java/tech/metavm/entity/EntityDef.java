package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.InstanceMap;
import tech.metavm.object.instance.InstanceRef;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.TypeReference;
import tech.metavm.util.ValuePlaceholder;

import javax.annotation.Nullable;
import java.util.function.Function;

public class EntityDef<T extends Entity> extends PojoDef<T> {

    public EntityDef(String name,
                     TypeReference<T> typeReference,
                     @Nullable PojoDef<? super T> parentDef,
                     Type type
    ) {
        this(name, typeReference.getType(), parentDef, type);
    }

    public EntityDef(String name,
                     Class<T> entityType,
                     @Nullable PojoDef<? super T> parentDef,
                     Type type
    ) {
        super(name, entityType, parentDef, type);
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
