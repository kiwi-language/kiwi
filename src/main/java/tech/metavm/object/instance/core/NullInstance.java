package tech.metavm.object.instance.core;

import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.Null;
import tech.metavm.util.WireTypes;

public class NullInstance extends PrimitiveInstance {

    public NullInstance(PrimitiveType type) {
        super(type);
    }

    @Override
    public Null getValue() {
        return null;
    }

    @Override
    public int getWireType() {
        return WireTypes.NULL;
    }

    @Override
    public Object toColumnValue() {
        return null;
    }

    @Override
    public String getTitle() {
        return "空";
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitNullInstance(this);
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return null;
    }
}
