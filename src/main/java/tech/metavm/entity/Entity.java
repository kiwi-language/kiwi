package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

public abstract class Entity implements Model, Identifiable, IdInitializing, RemovalAware {

    private boolean persisted;
    protected Long id;
    private long version;
    private long syncVersion;

    public Entity() {
    }

    public final Long getId() {
        return id;
    }

    @NoProxy
    public final EntityKey key() {
        if(id == null) {
            return null;
        }
        return EntityKey.create(this.getEntityType(), id);
    }

    @JsonIgnore
    public final boolean isIdNull() {
        return id == null;
    }

    @Override
    public final void initId(long id) {
        if(!isIdNull()) {
            throw new IllegalStateException("objectId is already initialized");
        }
//        if(StandardTypes.containsModel(this)) {
//            throw new InternalException("Initializing id for standard entity is not allowed");
//        }
        this.id = id;
    }

    @Override
    public void clearId() {
        this.id = null;
    }

    @JsonIgnore
    public long getVersion() {
        return version;
    }

    @JsonIgnore
    public long getSyncVersion() {
        return syncVersion;
    }

    @JsonIgnore
    @NoProxy
    public Class<? extends Entity> getEntityType() {
        return EntityUtils.getEntityType(this).asSubclass(Entity.class);
    }

    @Override
    public List<Object> onRemove() {
        return List.of();
    }

}
