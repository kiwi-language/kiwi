package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.instance.rest.PrimitiveInstanceParam;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class PrimitiveValue implements Value, Comparable<PrimitiveValue> {

    public PrimitiveValue() {
        super();
    }

    public abstract Object getValue();

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public PrimitiveInstanceParam getParam() {
        return new PrimitiveInstanceParam(
                getValueType().getKind().code(),
                getValue()
        );
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
        return Objects.equals(getValue(), that.getValue()) && Objects.equals(getValueType(), that.getValueType());
    }

    @Override
    public abstract PrimitiveType getValueType();

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getValueType());
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return new PrimitiveFieldValue(
                getTitle(),
                getValueType().getKind().code(),
                getValue()
        );
    }

    @Override
    public void writeInstance(MvOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NotNull PrimitiveValue o) {
        int cmp = Integer.compare(getValueType().getKind().code(), o.getValueType().getKind().code());
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
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.write(Objects.toString(getValue()));
    }

    @Override
    public Object toJson() {
        return getValue();
    }

    @Override
    public String toString() {
        return Objects.toString(getValue());
    }
}
