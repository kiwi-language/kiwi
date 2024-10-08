package org.metavm.entity;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Klass;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

public class EntityDef<T> extends PojoDef<T> {

    public EntityDef(Class<T> javaClass,
                     Type javaType,
                     @Nullable PojoDef<? super T> superDef,
                     Klass type,
                     DefContext defContext
    ) {
        super(javaClass, javaType, superDef, type, defContext);
    }

    @Override
    protected Id getId(T model) {
        return model instanceof Identifiable identifiable ? identifiable.tryGetId() : null;
    }

    @Override
    public boolean isProxySupported() {
        return true;
    }
}
