package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.Instances;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

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
    public void write(InstanceOutput output) {
        output.write(WireTypes.STRING);
        output.writeString(value);
    }

    public BooleanInstance contains(StringInstance that) {
        return Instances.createBoolean(value.contains(that.value));
    }

    public BooleanInstance startsWith(StringInstance that) {
        return Instances.createBoolean(value.startsWith(that.value));
    }

    public StringInstance concat(StringInstance that) {
        return new StringInstance(value + that.value, getType());
    }

    public BooleanInstance isBlank() {
        return Instances.createBoolean(NncUtils.isBlank(value));
    }

    @Override
    public StringInstance toStringInstance() {
        return this;
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitStringInstance(this);
    }

}
