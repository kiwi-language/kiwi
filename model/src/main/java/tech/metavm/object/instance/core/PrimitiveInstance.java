package tech.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.IEntityContext;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.instance.rest.PrimitiveInstanceParam;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;

import javax.annotation.Nullable;
import java.util.Objects;

public abstract class PrimitiveInstance extends Instance implements Comparable<PrimitiveInstance> {

    public PrimitiveInstance(PrimitiveType type) {
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
    public <R> void acceptReferences(InstanceVisitor<R> visitor) {
    }

    @Override
    public <R> void acceptChildren(InstanceVisitor<R> visitor) {
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
        PrimitiveInstance that = (PrimitiveInstance) o;
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
    public void writeRecord(InstanceOutput output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int compareTo(@NotNull PrimitiveInstance o) {
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
        treeWriter.writeLine(Objects.toString(getValue()));
    }

    @Override
    public Object toJson(IEntityContext context) {
        return getValue();
    }

    @Override
    public boolean isMutable() {
        return false;
    }
}
