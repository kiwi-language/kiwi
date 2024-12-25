package org.metavm.object.instance.core;

import org.metavm.entity.IEntityContext;
import org.metavm.object.instance.rest.InstanceParam;
import org.metavm.object.instance.rest.NullFieldValue;
import org.metavm.object.type.NullType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;

public class NullValue implements Value {

    public static final NullValue instance = new NullValue();

    public NullValue() {
    }

    @Override
    public NullType getType() {
        return NullType.instance;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.NULL);
    }

    @Override
    public Object toSearchConditionValue() {
        return null;
    }

    @Override
    public InstanceParam getParam() {
        return null;
    }

    @Override
    public String getTitle() {
        return "null";
    }

    @Override
    public void writeInstance(InstanceOutput output) {

    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitNullValue(this);
    }

    @Override
    public <R> void acceptReferences(ValueVisitor<R> visitor) {

    }

    @Override
    public <R> void acceptChildren(ValueVisitor<R> visitor) {

    }

    @Override
    public void writeTree(TreeWriter treeWriter) {

    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Object toJson(IEntityContext context) {
        return null;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public NullFieldValue toFieldValueDTO() {
        return NullFieldValue.instance;
    }

    @Override
    public boolean shouldSkipWrite() {
        return true;
    }

    @Nullable
    @Override
    public Id tryGetId() {
        return null;
    }

    @Override
    public int hashCode() {
        return NullValue.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NullValue;
    }

}
