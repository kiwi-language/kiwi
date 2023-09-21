package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.dto.RefDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public abstract class Entity implements Model, Identifiable, IdInitializing, RemovalAware, BindAware {

    private boolean persisted;
    private transient Long tmpId;
    @Nullable
    protected Long id;
    private long version;
    private long syncVersion;

    public Entity() {
    }

    public Entity(Long tmpId) {
        this.tmpId = tmpId;
    }

    @Nullable
    public final Long getId() {
        return id;
    }

    public final RefDTO getRef() {
        return new RefDTO(id, tmpId);
    }

    @NoProxy
    public final Long getIdRequired() {
        return NncUtils.requireNonNull(getId());
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

    public Long getTmpId() {
        return tmpId;
    }

    public void setTmpId(Long tmpId) {
        this.tmpId = tmpId;
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

    public void validate() {}

    @Override
    public List<Object> beforeRemove() {
        return List.of();
    }

    @Override
    public void onBind(IEntityContext context) {}

}
