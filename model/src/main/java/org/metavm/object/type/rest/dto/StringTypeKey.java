package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.metavm.entity.StdKlass;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Json
public record StringTypeKey() implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.STRING_TYPE);
    }

    @Override
    public String toTypeExpression() {
        return String.format("$$%s", StdKlass.string.get().getId());
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitStringTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.STRING_TYPE;
    }
}
