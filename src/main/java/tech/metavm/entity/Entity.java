package tech.metavm.entity;

import tech.metavm.object.meta.Field;
import tech.metavm.object.meta.Type;

import java.util.Objects;

public class Entity implements Identifiable {

    private boolean persisted;
    protected Long id;
    protected final EntityContext context;

    public Entity(Long id, EntityContext context) {
        this.context = context;
        if(id != null) {
            persisted = true;
            initId(id);
        }
    }

    public Entity(EntityContext context) {
        this(null, context);
    }

    public final Long getId() {
        return id;
    }

    public final EntityKey key() {
        if(id == null) {
            return null;
        }
        return new EntityKey(EntityUtils.getEntityType(this), id);
    }

    public final boolean isIdNull() {
        return id == null;
    }

    public final long getTenantId() {
        return context.getTenantId();
    }

    public EntityId getGlobalId() {
        return new EntityId(getClass(), id);
    }

    public final void initId(long id) {
        if(!isIdNull()) {
            throw new IllegalStateException("objectId is already initialized");
        }
        this.id = id;
    }

    protected void bind() {
        context.bind(this);
    }

    public boolean isPersisted() {
        return persisted;
    }

    public void setPersisted(boolean persisted) {
        this.persisted = persisted;
    }

    @Override
    public Entity copy() {
        return EntityUtils.copyEntity(this);
    }

    public EntityContext getContext() {
        return context;
    }


    public <T extends Entity> T getFromContext(Class<T> klass, long id) {
        return context.get(klass, id);
    }

    public Type getTypeFromContext(long typeId) {
        return context.getTypeRef(typeId);
    }

    public Field getFieldFromContext(long fieldId) {
        return context.getFieldRef(fieldId);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Entity e) {
            return EntityUtils.entityEquals(this, e);
        }
        else {
            return false;
        }
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
