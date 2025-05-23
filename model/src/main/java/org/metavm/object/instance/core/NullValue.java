package org.metavm.object.instance.core;

import org.metavm.object.type.NullType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public class NullValue implements Value {

    public static final NullValue instance = new NullValue();

    public NullValue() {
    }

    @Override
    public NullType getValueType() {
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
    public String getTitle() {
        return "null";
    }

    @Override
    public void writeInstance(MvOutput output) {

    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitNullValue(this);
    }

    @Override
    public void writeTree(TreeWriter treeWriter) {
        treeWriter.write("null");
    }

    @Override
    public Object toJson() {
        return null;
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean shouldSkipWrite() {
        return true;
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
