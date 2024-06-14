package org.metavm.object.instance.core;

import org.metavm.object.instance.rest.PrimitiveFieldValue;
import org.metavm.object.type.PrimitiveKind;
import org.metavm.object.type.PrimitiveType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.WireTypes;

public class PasswordInstance extends PrimitiveInstance {

    private final String value;

    public PasswordInstance(String value, PrimitiveType type) {
        super(type);
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(WireTypes.PASSWORD);
        output.writeString(value);
    }

    @Override
    public PrimitiveFieldValue toFieldValueDTO() {
        return new PrimitiveFieldValue(
                "******",
                PrimitiveKind.PASSWORD.code(),
                null
        );
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public <R> R accept(InstanceVisitor<R> visitor) {
        return visitor.visitPasswordInstance(this);
    }
}
