package org.metavm.object.instance.core;


import org.jetbrains.annotations.NotNull;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceFieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.instance.rest.ReferenceFieldValue;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/*
Migration procedure
1 Move the object and activate migration mode for the object.
2 New references will point to the old location and be flagged as forwarded.
3 Mark all current references as forwarded.
4 If a forwarded reference participates in a comparison, it will be resolved to its true form for the comparison if the initial comparison results in false.
5 Deactivate migration mode for the object, allowing new references to directly point to it.
6 Update existing references to point to the new location.
7 Remove the forwarding pointer.
 */
public class InstanceReference extends Instance {

    public static final Logger logger = LoggerFactory.getLogger(InstanceReference.class);

    private final @Nullable Id id;
    private @Nullable DurableInstance target;
    private final Supplier<DurableInstance> resolver;
    private boolean view;
    private boolean forwarded;

    public InstanceReference(DurableInstance resolved) {
        super(resolved.getType());
        this.target = resolved;
        this.id = resolved.tryGetId();
        resolver = () -> {
            throw new IllegalStateException("Already resolved");
        };
    }

    public InstanceReference(@Nullable Id id, Supplier<DurableInstance> resolver) {
        super(Types.getAnyType());
        this.id = id;
        this.resolver = resolver;
    }

    public DurableInstance resolve() {
        if (target == null)
            target = resolver.get();
        return target;
    }

    @Override
    public Type getType() {
        return resolve().getType();
    }

    @Override
    public Id getId() {
        return Objects.requireNonNull(tryGetId());
    }

    @Nullable
    @Override
    public String getStringId() {
        return NncUtils.get(tryGetId(), Id::toString);
    }

    public void setTarget(@NotNull DurableInstance target) {
        this.target = target;
    }

    public void forward(InstanceReference forwardPointer) {
        forwarded = true;
    }

    public boolean isForwarded() {
        return forwarded;
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Nullable
    @Override
    public Id tryGetId() {
        return id instanceof PhysicalId physicalId ? physicalId : resolve().tryGetId();
    }

    @Override
    public FieldValue toFieldValueDTO() {
        var target = resolve();
        if(target.isValue() || target.isArray() || (target instanceof ClassInstance clsInst && clsInst.isList()))
            return new InstanceFieldValue(resolve().getTitle(), resolve().toDTO());
        else {
            return new ReferenceFieldValue(
                    getTitle(),
                    Objects.requireNonNull(this.getStringIdForDTO(), "Id required"),
                    getType().toExpression());
        }
    }

    @Override
    public String getTitle() {
        return "Reference-" + id;
    }

    @Override
    public void writeRecord(InstanceOutput output) {
        resolve().writeRecord(output);
    }

    @Override
    public void write(InstanceOutput output) {
        if (isValueReference())
            resolve().writeRecord(output);
        else {
            output.write(forwarded ? WireTypes.FORWARDED_REFERENCE : WireTypes.REFERENCE);
            output.writeId(id instanceof PhysicalId ? id : resolve().getId());
        }
    }

    @Override
    public Object toSearchConditionValue() {
        return getStringId();
    }

    @Override
    protected InstanceParam getParam() {
        return resolve().getParam();
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitInstanceReference(this);
    }

    @Override
    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
        this.accept(visitor);
    }

    @Override
    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
    }

    @Override
    protected void writeTree(TreeWriter treeWriter) {
        treeWriter.writeLine(getStringId());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object toJson(IEntityContext context) {
        return getStringId();
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public boolean isRemoved() {
        return isLoaded() && resolve().isRemoved();
    }

    public Long tryGetTreeId() {
        return NncUtils.get(id, Id::tryGetTreeId);
    }

    public boolean isLoaded() {
        return target != null;
    }

//    public IInstanceContext getContext() {
//        return target != null ? target.getContext() : context;
//    }

//    public void setContext(IInstanceContext context) {
//        this.context = context;
//    }

//    public Object getMappedEntity() {
//        return target != null ? target.getMappedEntity() : mappedEntity;
//    }

//    public void setMappedEntity(Object mappedEntity) {
//        if (target != null)
//            target.setMappedEntity(mappedEntity);
//        else
//            this.mappedEntity = mappedEntity;
//    }

    public long getTreeId() {
        return Objects.requireNonNull(id).getTreeId();
    }

    public boolean isNew() {
        return isInitialized() && resolve().isNew();
    }

    public boolean isInitialized() {
        return isLoaded();
    }

    public void ensureLoaded() {
        if (!isLoaded())
            resolve();
    }

    public int getSeq() {
        return resolve().getSeq();
    }

    public boolean isIdInitialized() {
        return id instanceof PhysicalId || resolve().tryGetId() instanceof PhysicalId;
    }

    @Override
    public boolean isArray() {
        return resolve() instanceof ArrayInstance;
    }

    @Override
    public boolean isObject() {
        return resolve() instanceof ClassInstance;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InstanceReference that) {
            if (id != null && id.equals(that.id))
                return true;
            if (id == null || that.id == null || forwarded || that.forwarded)
                return resolve() == that.resolve();
            return false;
        } else
            return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean shouldSkipWrite() {
        return target != null && target.isEphemeral();
    }

    public boolean isValueReference() {
        return id == null && resolve().isValue();
    }

}
