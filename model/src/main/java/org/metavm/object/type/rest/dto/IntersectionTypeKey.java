package org.metavm.object.type.rest.dto;

import org.metavm.object.type.IntersectionType;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import java.util.Set;

public record IntersectionTypeKey(Set<TypeKey> typeKeys) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.INTERSECTION);
        output.writeInt(typeKeys.size());
        typeKeys.forEach(t -> t.write(output));
    }

    @Override
    public String toTypeExpression() {
        return NncUtils.join(typeKeys, TypeKey::toTypeExpression, "&");
    }

    @Override
    public IntersectionType toType(TypeDefProvider typeDefProvider) {
        return new IntersectionType(NncUtils.mapUnique(typeKeys, k -> k.toType(typeDefProvider)));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitIntersectionTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.INTERSECTION;
    }
}
