package org.metavm.object.instance.core;

import org.metavm.object.type.PrimitiveType;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public class PasswordValue extends PrimitiveValue {

    private final String value;

    public PasswordValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public PrimitiveType getValueType() {
        return PrimitiveType.passwordType;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.PASSWORD);
        output.writeUTF(value);
    }

    @Override
    public String getTitle() {
        return value;
    }

    @Override
    public <R> R accept(ValueVisitor<R> visitor) {
        return visitor.visitPasswordValue(this);
    }
}
