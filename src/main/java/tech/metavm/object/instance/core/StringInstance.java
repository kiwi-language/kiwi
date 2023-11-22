package tech.metavm.object.instance.core;

import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.InstanceUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.WireTypes;

public class StringInstance extends PrimitiveInstance {

    private final String value;

    public StringInstance(String value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public int getWireType() {
        return WireTypes.STRING;
    }

    @Override
    public Object toColumnValue() {
        return value;
    }

    public BooleanInstance contains(StringInstance that) {
        return InstanceUtils.createBoolean(value.contains(that.value));
    }

    public BooleanInstance startsWith(StringInstance that) {
        return InstanceUtils.createBoolean(value.startsWith(that.value));
    }

    public StringInstance concat(StringInstance that) {
        return new StringInstance(value + that.value, getType());
    }

    public BooleanInstance isBlank() {
        return InstanceUtils.createBoolean(NncUtils.isBlank(value));
    }

    @Override
    public void writeTo(InstanceOutput output, boolean includeChildren) {
        output.writeString(value);
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public void accept(InstanceVisitor visitor) {
        visitor.visitStringInstance(this);
    }

}
