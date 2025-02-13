package org.metavm.object.instance.core;


import org.jetbrains.annotations.NotNull;
import org.metavm.flow.ClosureContext;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.instance.rest.ReferenceFieldValue;
import org.metavm.object.type.Type;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/*
Relocation process
1 Move the object and activate migration mode for the object.
2 New references will point to the old location and be flagged as forwarded.
3 Mark all current references as forwarded.
4 If a forwarded reference participates in a comparison, it will be resolved to its true form for the comparison if the initial comparison results in false.
5 Deactivate migration mode for the object, allowing new references to directly point to it.
6 Update existing references to point to the new location.
7 Remove the forwarding pointer.
 */
public class Reference implements Value {

    public static final int FLAG_FORWARDED = 1;
    public static final int FLAG_EAGER = 2;

    public static final Logger logger = LoggerFactory.getLogger(Reference.class);

    private @Nullable Id id;
    private @Nullable Instance target;
    private final Supplier<Instance> resolver;
    private int flags = 0;

    public Reference(Instance resolved) {
        this.target = resolved;
        this.id = resolved.tryGetId();
        resolver = () -> {
            throw new IllegalStateException("Already resolved");
        };
    }

    public Reference(@NotNull Id id, Supplier<Instance> resolver) {
        super();
        this.id = id;
        this.resolver = resolver;
    }

    public Instance get() {
        if (target == null) {
            target = Objects.requireNonNull(resolver.get(), () -> "Failed to resolve reference " + id);
        }
        return target;
    }

    @Override
    public Type getValueType() {
        return get().getInstanceType();
    }

    @Override
    public Id getId() {
        return Objects.requireNonNull(tryGetId());
    }

    @Nullable
    @Override
    public String getStringId() {
        return Utils.safeCall(tryGetId(), Id::toString);
    }

    public void setTarget(@NotNull Instance target) {
        this.target = target;
    }

    public boolean isForwarded() {
        return (flags & FLAG_FORWARDED) != 0;
    }

    @Nullable
    @Override
    public Id tryGetId() {
        return id instanceof PhysicalId physicalId ? physicalId : get().tryGetId();
    }

    @Override
    public FieldValue toFieldValueDTO() {
        var target = get();
//        if(target.isInlineValue() || target.isArray() || (target instanceof ClassInstance clsInst && clsInst.isList()))
//            return new InstanceFieldValue(resolve().getTitle(), resolve().toDTO());
//        else {
            return new ReferenceFieldValue(
                    getTitle(),
                    Objects.requireNonNull(this.getStringIdForDTO(),
                            () -> "Instance " + this.resolveDurable() + " has an uninitialized ID"),
                    getValueType().toExpression());
//        }
    }

    @Override
    public String getTitle() {
        return "Reference-" + tryGetId();
    }

    @Override
    public void writeInstance(MvOutput output) {
        resolveDurable().write(output);
    }

    @Override
    public void write(MvOutput output) {
        if (isInlineValueReference())
            writeInstance(output);
        else {
            if(flags != 0) {
                output.write(WireTypes.FLAGGED_REFERENCE);
                output.write(flags);
            }
            else
                output.write(WireTypes.REFERENCE);
            output.writeReference(this);
        }
    }

    @Override
    public Object toSearchConditionValue() {
        return getStringId();
    }

    @Override
    public InstanceParam getParam() {
        return get().getParam();
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitReference(this);
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.write(get().toString());
    }

    @Override
    public Object toJson() {
        return getStringId();
    }

    public boolean isRemoved() {
        return isLoaded() && get().isRemoved();
    }

    public Long tryGetTreeId() {
        return id != null ? id.tryGetTreeId() : null;
    }

    public boolean isLoaded() {
        return target != null;
    }

    public void setForwarded() {
        flags |= FLAG_FORWARDED;
    }

    public void clearForwarded() {
        flags &= ~FLAG_FORWARDED;
    }

    public boolean isEager() {
        return (this.flags & FLAG_EAGER) != 0;
    }

    public void setEager() {
        this.flags |= FLAG_EAGER;
    }

    public void clearEager() {
        flags &= ~FLAG_EAGER;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Reference forward() {
        assert isForwarded();
        var r = get().getReference();
        r.setFlags(flags & ~FLAG_FORWARDED);
        return r;
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
        return (id == null || isInitialized()) && get().isNew();
    }

    public boolean isInitialized() {
        return isLoaded();
    }

    public void ensureLoaded() {
        if (!isLoaded())
            get();
    }

    public int getSeq() {
        return get().getSeq();
    }

    public boolean isIdInitialized() {
        return id instanceof PhysicalId || get().tryGetId() instanceof PhysicalId;
    }

    @Override
    public boolean isArray() {
        return get() instanceof ArrayInstance;
    }

    @Override
    public boolean isObject() {
        return get() instanceof ClassInstance;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        // TODO refactor this mess
        if (obj instanceof Reference that) {
            that = that.tryRedirect();
            var id = this.tryGetId();
            var thatId = that.tryGetId();
            if (!(id instanceof PhysicalId) && !(thatId instanceof PhysicalId))
                return get() == that.get();
            if (id != null && id.equals(thatId))
                return true;
            if (id == null || thatId == null || isForwarded() || that.isForwarded())
                return get() == that.get();
        }
        return false;
    }

    public Reference tryRedirect() {
        return this;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : get().hashCode();
    }

    @Override
    public boolean shouldSkipWrite() {
        return target != null && target.isEphemeral();
    }

    public boolean isValueReference() {
        return (id == null || target != null) && get().isValue();
    }

    public boolean isInlineValueReference() {
        return id == null && get().isInlineValue();
    }

    public boolean isResolved() {
        return target != null;
    }

    public boolean idEquals(Id id) {
        return this.id != null && this.id.equals(id);
    }

    public int getFlags() {
        return flags;
    }

    @Override
    public ClosureContext getClosureContext() {
        var r = get();
        return r instanceof ClassInstance obj ? obj.getClosureContext() : null;
    }

    public void refreshId() {
        if (!(id instanceof PhysicalId))
            id = get().tryGetId();
    }

}
