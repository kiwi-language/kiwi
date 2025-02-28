package org.metavm.object.instance.core;


import org.jetbrains.annotations.NotNull;
import org.metavm.flow.ClosureContext;
import org.metavm.object.instance.rest.FieldValue;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.instance.rest.ReferenceFieldValue;
import org.metavm.object.type.Type;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class EntityReference implements Reference {

    private final @NotNull Id id;
    private @Nullable Instance target;
    private final Supplier<Instance> resolver;

    public EntityReference(Instance resolved) {
        assert !resolved.isValue();
        this.target = resolved;
        this.id = resolved.getId();
        resolver = () -> {
            throw new IllegalStateException("Already resolved");
        };
    }

    public EntityReference(@NotNull Id id, Supplier<Instance> resolver) {
        super();
        this.id = id;
        this.resolver = resolver;
    }

    @Override
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

    public @NotNull Id getId() {
        return id;
    }

    public String getStringId() {
        return id.toString();
    }

    @Override
    public FieldValue toFieldValueDTO() {
        return new ReferenceFieldValue(
                getTitle(),
                id.toString(),
                getValueType().toExpression());
    }

    @Override
    public String getTitle() {
        return "Reference-" + id;
    }

    @Override
    public void writeInstance(MvOutput output) {
        resolveDurable().write(output);
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.REFERENCE);
        output.writeReference(this);
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

    @Override
    public boolean isRemoved() {
        return isResolved() && get().isRemoved();
    }

    public Long tryGetTreeId() {
        return id.tryGetTreeId();
    }

    public long getTreeId() {
        return Objects.requireNonNull(id).getTreeId();
    }

    public boolean isNew() {
        return isResolved() && get().isNew();
    }

    public boolean isIdInitialized() {
        return id instanceof PhysicalId;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public String toString() {
        return getTitle();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityReference that && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean shouldSkipWrite() {
        return target != null && target.isEphemeral();
    }

    public boolean isResolved() {
        return target != null;
    }

    @Override
    public ClosureContext getClosureContext() {
        var r = get();
        return r instanceof ClassInstance obj ? obj.getClosureContext() : null;
    }

    @Override
    public InstanceDTO toDTO() {
        return toDTO(id.toString(), getParam());
    }
}
