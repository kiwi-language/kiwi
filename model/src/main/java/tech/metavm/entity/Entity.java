package tech.metavm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.Value;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.PhysicalId;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

public abstract class Entity implements Model, Identifiable, IdInitializing, RemovalAware, BindAware {

    public static final Logger logger = LoggerFactory.getLogger(Entity.class);

    private transient boolean removed;
    private transient boolean persisted;
    //    private transient Long tmpId;
    @Nullable
    protected transient Id id;
    @Nullable
    private transient Entity parentEntity;
    @Nullable
    private transient Field parentEntityField;
    private transient long version;
    private transient long syncVersion;
    private transient boolean ephemeralEntity;
    private transient boolean strictEphemeral;

    public Entity() {
        this(null);
    }

    public Entity(Long tmpId) {
        this(tmpId, null);
    }

    public Entity(Long tmpId, @Nullable EntityParentRef parentRef) {
        this(tmpId, parentRef, false);
    }

    public Entity(Long tmpId, @Nullable EntityParentRef parentRef, boolean ephemeral) {
        this.id = tmpId != null ? TmpId.of(tmpId) : null;
        this.ephemeralEntity = ephemeral;
        assert !ephemeral || !(this instanceof Value) : "Can not set a value object to ephemeral";
        if (parentRef != null) {
            if (parentRef.parent() instanceof ReadonlyArray<?> array) {
                NncUtils.requireNull(parentRef.field());
                NncUtils.requireTrue(array.getElementClass().isAssignableFrom(getClass()));
            } else {
                NncUtils.requireNonNull(parentRef.field());
                NncUtils.requireTrue(parentRef.field().getType().isAssignableFrom(getClass()));
                this.parentEntityField = parentRef.field();
            }
            this.parentEntity = parentRef.parent();
        }
    }

//    @Nullable
//    public final Long tryGetId() {
//        return id;
//    }

    @NoProxy
    public boolean idEquals(Id id) {
        return Objects.equals(this.id, id);
    }

    @NoProxy
    public boolean isIdNotNull() {
        return id != null;
    }

//    public final RefDTO getRef() {
//        return new RefDTO(id, tmpId, ModelDefRegistry.getTypeId(this.getClass()));
//    }

    @NoProxy
    public boolean isEphemeralEntity() {
        return ephemeralEntity;
    }

    public void setEphemeralEntity(boolean ephemeralEntity) {
        assert !ephemeralEntity || !(this instanceof Value) : "can not set a value object to ephemeral";
        this.ephemeralEntity = ephemeralEntity;
        if (ephemeralEntity) {
            EntityUtils.forEachDescendant(this, e -> {
                if (e instanceof Entity entity)
                    entity.ephemeralEntity = true;
            });
        }
    }

    public boolean isStrictEphemeral() {
        return strictEphemeral;
    }

    public void setStrictEphemeral(boolean strictEphemeral) {
        assert !strictEphemeral || !(this instanceof Value) : "can not set a value object to ephemeral";
        this.strictEphemeral = strictEphemeral;
        if(strictEphemeral) {
            this.ephemeralEntity = true;
            EntityUtils.forEachDescendant(this, e -> {
                if(e instanceof Entity entity)
                    entity.ephemeralEntity = entity.strictEphemeral = true;
            });
        }
    }

//    @NoProxy
//    public final long getId() {
//        if (id != null)
//            return id;
//        else
//            throw new InternalException("Entity id not initialized yet: " + this);
//    }

    public Id getEntityId() {
//        var type = ModelDefRegistry.getType(this.getClass());
//        return PhysicalId.of(getId(), type.getCategory(), type.getEntityId().getPhysicalId());
        return id;
    }

    @Nullable
    @Override
    @NoProxy
    public Id tryGetId() {
        return id;
    }

    public String getStringId() {
        return NncUtils.get(getEntityId(), Id::toString);
    }

    @NoProxy
    public final EntityKey key() {
        if (id == null) {
            return null;
        }
        return EntityKey.create(this.getEntityType(), id);
    }

    public final boolean isRemoved() {
        return removed;
    }

    public final void ensureNotRemoved() {
        if (removed)
            throw new InternalException(String.format("'%s' is already removed", this));
    }

    final void setRemoved() {
        ensureNotRemoved();
        this.removed = true;
    }

    @Nullable
    public Entity getParentEntity() {
        return parentEntity;
    }

    public Entity getRootEntity() {
        Entity root = this;
        while (root.parentEntity != null)
            root = root.parentEntity;
        return root;
    }

    public <T extends Entity> T addChild(T child, @Nullable String fieldName) {
        NncUtils.requireNonNull(child, "Child object can not be null");
        var field = fieldName != null ?
                ReflectionUtils.getDeclaredFieldRecursively(this.getClass(), fieldName) : null;
        child.setParent(this, field);
        return child;
    }

    void setParent(Entity parent, @Nullable Field parentField) {
        if (this.parentEntity != null) {
            if (!Objects.equals(parent, parentEntity) || !Objects.equals(this.parentEntityField, parentField))
                throw new InternalException(
                        String.format("Can not change parent. entity: %s, currentParent: %s, newParent: %s",
                                EntityUtils.getEntityDesc(this),
                                EntityUtils.getEntityPath(this.parentEntity),
                                EntityUtils.getEntityPath(parent)));
        } else {
            this.parentEntity = parent;
            this.parentEntityField = parentField;
            if (parent != null) {
                if(parent.isStrictEphemeral() && !strictEphemeral)
                    forEachDescendant(e -> e.strictEphemeral = e.ephemeralEntity = true);
                else if(parent.isEphemeralEntity() && !ephemeralEntity)
                    forEachDescendant(e -> e.ephemeralEntity = true);
                else if(!parent.isEphemeralEntity() && strictEphemeral) {
                    throw new InternalException("Can not add a strict ephemeral child to a non-ephemeral entity. parent: " + EntityUtils.getEntityPath(parent)
                            + ", child: " + EntityUtils.getEntityDesc(this));
                }
            }
        }
    }

    public void forEachReference(Consumer<Object> action) {
        var desc = DescStore.get(EntityUtils.getRealType(this));
        for (var prop : desc.getNonTransientProps()) {
            var ref = prop.get(this);
            if (ref != null && !ValueUtil.isPrimitive(ref)) {
                if(DebugEnv.recordPath) {
                    EntityUtils.enterPathItem(prop.getName());
                    action.accept(ref);
                    EntityUtils.exitPathItem();
                }
                else
                    action.accept(ref);
            }
        }
    }

    public void forEachDescendant(Consumer<Entity> action) {
        forEachDescendant(action, false);
    }

    public void forEachDescendant(Consumer<Entity> action, boolean skipCopyIgnore) {
        action.accept(this);
        var desc = DescStore.get(EntityUtils.getRealType(this));
        desc.forEachNonTransientProp(prop -> {
            if (prop.isChildEntity()) {
                if(skipCopyIgnore && prop.isCopyIgnore())
                    return;
                var child = (Entity) prop.get(this);
                if (child != null)
                    child.forEachDescendant(action, skipCopyIgnore);
            }
        });
    }

    public Map<Field, Object> getChildMap() {
        var desc = DescStore.get(EntityUtils.getRealType(this));
        Map<Field, Object> childMap = new HashMap<>();
        desc.forEachNonTransientProp(prop -> {
            if (prop.isChildEntity()) {
                var child = (Entity) prop.get(this);
                if (child != null)
                    childMap.put(prop.getField(), child);
            }
        });
        return childMap;
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

    @NoProxy
    public final boolean hasPhysicalId() {
        return id instanceof PhysicalId;
    }

    @Override
    public final void initId(Id id) {
        if (tryGetPhysicalId() != null) {
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
        if (visited.contains(this))
            throw new InternalException("Circular reference in entity structure");
        descendants.add(this);
        visited.add(this);
        var desc = DescStore.get(EntityUtils.getRealType(getClass()));
        for (EntityProp prop : desc.getPropsWithAnnotation(ChildEntity.class)) {
            var child = prop.get(this);
            if (child != null) {
                if (child instanceof Entity childEntity)
                    childEntity.getDescendants(descendants, visited);
                else
                    descendants.add(child);
            }
        }
    }

//    @NoProxy
//    public Long getTmpId() {
//        return tmpId;
//    }

//    public void setTmpId(Long tmpId) {
//        this.tmpId = tmpId;
//    }

    @Override
    public void clearId() {
        this.id = null;
    }

    @JsonIgnore
    public long getEntityVersion() {
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
    public List<Object> beforeRemove(IEntityContext context) {
        return List.of();
    }

    @Override
    public void onBind(IEntityContext context) {
    }

    public boolean afterContextInitIds() {
        return false;
    }

    @Override
    @NoProxy
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    @NoProxy
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    @NoProxy
    public final String toString() {
        if (EntityUtils.isModelInitialized(this))
            return toString0();
        else
            return String.format("Uninitialized entity, id: %s", id);
    }

    protected String toString0() {
        return String.format("%s, id: %s",
                EntityUtils.getRealType(this).getSimpleName(), id);
    }

    @NoProxy
    public Long getTmpId() {
        if (id instanceof TmpId tmpId)
            return tmpId.getTmpId();
        else
            return null;
    }

    public void setTmpId(Long tmpId) {
        initId(tmpId != null ? TmpId.of(tmpId) : null);
    }
}
