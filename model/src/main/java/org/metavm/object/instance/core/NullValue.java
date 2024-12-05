package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;
import org.metavm.util.Null;
import org.metavm.util.WireTypes;

public class NullValue extends PrimitiveValue {

    public NullValue() {
    }

    @Override
    public Null getValue() {
        return null;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.nullType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.NULL);
    }

    @Override
    public String getTitle() {
        return "null";
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitNullValue(this);
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return PrimitiveFieldValue.NULL;
    }

    @Override
    public boolean shouldSkipWrite() {
        return true;
    }
}
