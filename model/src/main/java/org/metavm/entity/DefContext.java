package org.metavm.entity;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.Klass;

import java.util.Collection;
import java.util.List;

@Slf4j
public abstract class DefContext implements IInstanceContext {

    public DefContext() {
    }

    public abstract Id getModelId(Object o);

    public abstract Klass getKlass(Class<?> javaClass);

    public abstract Collection<Entity> entities();


    @Override
    public <T extends Entity> List<T> query(EntityIndexQuery<T> query) {
        return List.of();
    }

    @Override
    public long count(EntityIndexQuery<?> query) {
        return 0;
    }

    @Override
    public boolean isMigrating() {
        return false;
    }

    @Override
    public void dumpContext() {
        for (Entity entity : entities()) {
            log.trace("Entity {}-{}", entity.getClass().getName(), entity.getId());
        }
    }
}
