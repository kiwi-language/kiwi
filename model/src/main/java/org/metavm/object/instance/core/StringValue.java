package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

public class StringValue extends PrimitiveValue {

    public final String value;

    public StringValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public PrimitiveType getType() {
        return PrimitiveType.stringType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.STRING);
        output.writeUTF(value);
    }

    public BooleanValue contains(StringValue that) {
        return Instances.createBoolean(value.contains(that.value));
    }

    public BooleanValue startsWith(StringValue that) {
        return Instances.createBoolean(value.startsWith(that.value));
    }

    public BooleanValue endsWith(StringValue that) {
        return Instances.createBoolean(value.endsWith(that.value));
    }

    public StringValue concat(StringValue that) {
        return new StringValue(value + that.value);
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

    @Override
    public String stringValue() {
        return value;
    }
}
