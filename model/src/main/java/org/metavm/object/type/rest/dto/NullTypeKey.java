package org.metavm.object.type.rest.dto;

import org.metavm.object.type.NullType;
import org.metavm.object.type.Type;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

public record NullTypeKey() implements TypeKey {

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.NULL_TYPE);
    }

    @Override
    public String toTypeExpression() {
        return "null";
    }

    @Override
    public Type toType(TypeDefProvider typeDefProvider) {
        return NullType.instance;
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitNullTypeKey(this);
    }

    @Override
    public int getCode() {
        return 0;
    }
}
