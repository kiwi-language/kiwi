package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.object.meta.StandardTypes;
import tech.metavm.util.InternalException;

public abstract class Entity implements Model, Identifiable, IdInitializing {

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
    public Class<? extends Entity> getEntityType() {
        return EntityUtils.getEntityType(this);
    }

//    @Override
//    public final boolean equals(Object o) {
//        if (this == o) return true;
//        if(o instanceof Entity e) {
//            return EntityUtils.entityEquals(this, e);
//        }
//        else {
//            return false;
//        }
//    }

    public void remove() {}

//    @Override
//    public final int hashCode() {
//        if(id == null) {
//            return super.hashCode();
//        }
//        else {
//            return Objects.hash(id);
//        }
//    }
}
