package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.Instances;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

public class StringValue extends PrimitiveValue {

    public final String value;

    public StringValue(@NotNull String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
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
        return Instances.createBoolean(Utils.isBlank(value));
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
