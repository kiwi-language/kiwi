package org.metavm.object.type.rest.dto;

import org.metavm.object.type.NeverType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record NeverTypeKey() implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.NEVER_TYPE);
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
        return WireTypes.NEVER_TYPE;
    }
}
