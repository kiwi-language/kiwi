package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.dto.RefDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public abstract class Entity implements Model, Identifiable, IdInitializing, RemovalAware, BindAware {

    private boolean persisted;
    private transient Long tmpId;
    @Nullable
    protected Long id;
    @Nullable
    private transient Entity parentEntity;
    @Nullable
    private transient Field parentEntityField;
    private long version;
    private long syncVersion;

    public Entity() {
    }

    public Entity(Long tmpId) {
        this(tmpId, null);
    }

    public Entity(Long tmpId, @Nullable EntityParentRef parentRef) {
        this.tmpId = tmpId;
        if(parentRef != null) {
            if(parentRef.parent() instanceof ReadonlyArray<?> array) {
                NncUtils.requireNull(parentRef.field());
                NncUtils.requireTrue(array.getElementClass().isAssignableFrom(getClass()));
            }
            else {
                NncUtils.requireNonNull(parentRef.field());
                NncUtils.requireTrue(parentRef.field().getType().isAssignableFrom(getClass()));
                this.parentEntityField = parentRef.field();
            }
            this.parentEntity = parentRef.parent();
        }
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

    @Nullable
    public Entity getParentEntity() {
        return parentEntity;
    }

    public <T extends Entity> T addChild(T child, @Nullable String fieldName) {
        var field = fieldName != null ?
                ReflectUtils.getDeclaredFieldRecursively(this.getClass(), fieldName) : null;
        child.initParent(this, field);
        return child;
    }

    void initParent(Entity parent, @Nullable Field parentField) {
//        if(this.parent != null) {
//            throw new InternalException("Can not reInit parent");
//        }
        this.parentEntity = parent;
        this.parentEntityField = parentField;
    }

    @Nullable
    public Field getParentEntityField() {
        return parentEntityField;
    }

    @JsonIgnore
    @NoProxy
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

    public List<Object> getDescendants() {
        var children = new ArrayList<>();
        getDescendants(children, new IdentitySet<>());
        return children;
    }

    protected void getDescendants(List<Object> descendants, IdentitySet<Object> visited) {
        if(visited.contains(this))
            throw new InternalException("Circular reference in entity structure");
        descendants.add(this);
        visited.add(this);
        var desc = DescStore.get(EntityUtils.getRealType(getClass()));
        for (EntityProp prop : desc.getPropsWithAnnotation(ChildEntity.class)) {
            var child = prop.get(this);
            if(child != null) {
                if(child instanceof Entity childEntity)
                    childEntity.getDescendants(descendants, visited);
                else
                    descendants.add(child);
            }
        }
    }

    @NoProxy
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
    public List<Object> beforeRemove(IEntityContext context) {
        return List.of();
    }

    @Override
    public void onBind(IEntityContext context) {}

    public boolean afterContextInitIds() {
        return false;
    }

}
