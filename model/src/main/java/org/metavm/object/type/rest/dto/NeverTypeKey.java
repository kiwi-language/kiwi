package org.metavm.object.type.rest.dto;

import org.metavm.object.type.NeverType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.InstanceOutput;

public record NeverTypeKey() implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.NEVER);
    }

    @Override
    public String toTypeExpression() {
        return "never";
    }

    @Override
    public NeverType toType(TypeDefProvider typeDefProvider) {
        return NeverType.instance;
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitNeverTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.NEVER;
    }
}
