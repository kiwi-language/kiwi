package org.metavm.object.type.rest.dto;

import org.jsonk.Json;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import java.util.Set;

@Json
public record IntersectionTypeKey(Set<TypeKey> typeKeys) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.INTERSECTION_TYPE);
        output.writeInt(typeKeys.size());
        typeKeys.forEach(t -> t.write(output));
    }

    @Override
    public String toTypeExpression() {
        return Utils.join(typeKeys, TypeKey::toTypeExpression, "&");
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitIntersectionTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.INTERSECTION_TYPE;
    }
}
