package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

public class StringValue extends PrimitiveValue {

    private final String value;

    public StringValue(String value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.STRING);
        output.writeString(value);
    }

    public BooleanValue contains(StringValue that) {
        return Instances.createBoolean(value.contains(that.value));
    }

    public BooleanValue startsWith(StringValue that) {
        return Instances.createBoolean(value.startsWith(that.value));
    }

    public StringValue concat(StringValue that) {
        return new StringValue(value + that.value, getType());
    }

    public BooleanValue isBlank() {
        return Instances.createBoolean(NncUtils.isBlank(value));
    }

    @Override
    public StringValue toStringInstance() {
        return this;
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitStringValue(this);
    }

}
