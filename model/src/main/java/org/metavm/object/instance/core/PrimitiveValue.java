package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.instance.rest.PrimitiveInstanceParam;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class PrimitiveValue extends Value implements Comparable<PrimitiveValue> {

    public PrimitiveValue(PrimitiveType type) {
        super(type);
    }

    public abstract Object getValue();

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    protected PrimitiveInstanceParam getParam() {
        return new PrimitiveInstanceParam(
                getType().getKind().code(),
                getValue()
        );
    }

    @Override
    public <R> void acceptReferences(ValueVisitor<R> visitor) {
    }

    @Override
    public <R> void acceptChildren(ValueVisitor<R> visitor) {
    }

    @Override
    public @Nullable Id tryGetId() {
        return null;
    }

    @Override
    public Object toSearchConditionValue() {
        return getValue();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveValue that = (PrimitiveValue) o;
        return Objects.equals(getValue(), that.getValue()) && Objects.equals(getType(), that.getType());
    }

    @Override
    public PrimitiveType getType() {
        return (PrimitiveType) super.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getType());
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return new PrimitiveFieldValue(
                getTitle(),
                getType().getKind().code(),
                getValue()
        );
    }

    @Override
    public void writeInstance(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NotNull PrimitiveValue o) {
        int cmp = Integer.compare(getType().getKind().code(), o.getType().getKind().code());
        if(cmp != 0)
            return cmp;
        //noinspection rawtypes
        var v1 = (Comparable) getValue();
        //noinspection rawtypes
        var v2 = (Comparable) o.getValue();
        if(v1 == null && v2 == null)
            return 0;
        if(v1 == null)
            return -1;
        if(v2 == null)
            return 1;
        //noinspection unchecked
        return v1.compareTo(v2);
    }

    @Override
    protected void writeTree(TreeWriter treeWriter) {
        treeWriter.write(Objects.toString(getValue()));
    }

    @Override
    public Object toJson(IEntityContext context) {
        return getValue();
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public String toString() {
        return Objects.toString(getValue());
    }
}
