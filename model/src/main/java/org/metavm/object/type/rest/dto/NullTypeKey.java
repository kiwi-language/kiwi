package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Json
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
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitNullTypeKey(this);
    }

    @Override
    public int getCode() {
        return 0;
    }
}
