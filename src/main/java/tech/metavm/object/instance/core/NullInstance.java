package tech.metavm.object.instance.core;

import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.Null;

public class NullInstance extends PrimitiveInstance {

    public NullInstance(PrimitiveType type) {
        super(type);
    }

    @Override
    public Null getValue() {
        return null;
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
    public PrimitiveFieldValue toFieldValueDTO() {
        return null;
    }
}
