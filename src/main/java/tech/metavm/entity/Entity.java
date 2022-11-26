package tech.metavm.entity;

import java.util.Objects;

public abstract class Entity implements Model, Identifiable {

    private boolean persisted;
    protected Long id;
    private long version;
    private long syncVersion;

    public Entity() {
    }

    public final Long getId() {
        return id;
    }

    public final EntityKey key() {
        if(id == null) {
            return null;
        }
        return EntityKey.create(this.getEntityType(), id);
    }

    public final boolean isIdNull() {
        return id == null;
    }

    public final void initId(long id) {
        if(!isIdNull()) {
            throw new IllegalStateException("objectId is already initialized");
        }
        this.id = id;
    }

    @Override
    public Entity copy() {
        return EntityUtils.copyEntity(this);
    }

    public long getVersion() {
        return version;
    }

    public long getSyncVersion() {
        return syncVersion;
    }

    public Class<? extends Entity> getEntityType() {
        return EntityUtils.getEntityType(this);
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

    public void remove() {}

    @Override
    public final int hashCode() {
        if(id == null) {
            return super.hashCode();
        }
        else {
            return Objects.hash(id);
        }
    }
}
