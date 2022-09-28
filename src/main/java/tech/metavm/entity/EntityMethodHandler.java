package tech.metavm.entity;

import javassist.util.proxy.MethodHandler;

import java.lang.reflect.Method;

public final class EntityMethodHandler implements MethodHandler {

    private final EntityContext context;
    private final Class entityType;
    private final long entityId;

    EntityMethodHandler(Class<?> entityType, long entityId, EntityContext context) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.context = context;
    }

    @Override
    public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
        Entity entity = getEntity();
        if (entity == null) {
            throw new RuntimeException("Entity " + entityType.getName() + ":" + entityId + " not found");
        }
        thisMethod.setAccessible(true);
        return thisMethod.invoke(entity, args);
    }

    public Entity getEntity() {
        return context.get(entityType, entityId);
    }

    public Class<?> getEntityType() {
        return entityType;
    }

    public long getEntityId() {
        return entityId;
    }
}
