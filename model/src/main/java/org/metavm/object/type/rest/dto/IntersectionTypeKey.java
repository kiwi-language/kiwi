package org.metavm.object.type.rest.dto;

import org.metavm.object.type.IntersectionType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import java.util.Set;

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
    public IntersectionType toType(TypeDefProvider typeDefProvider) {
        return new IntersectionType(Utils.mapToSet(typeKeys, k -> k.toType(typeDefProvider)));
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
