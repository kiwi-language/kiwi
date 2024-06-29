package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Null;
import org.metavm.util.WireTypes;

public class NullInstance extends PrimitiveInstance {

    public NullInstance(PrimitiveType type) {
        super(type);
    }

    @Override
    public Null getValue() {
        return null;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.NULL);
    }

    @Override
    public String getTitle() {
        return "null";
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitNullInstance(this);
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
