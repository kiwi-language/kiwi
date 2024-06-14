package org.metavm.object.type.rest.dto;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.UnionType;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import java.util.Set;

public record UnionTypeKey(Set<TypeKey> memberKeys) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.UNION);
        output.writeInt(memberKeys.size());
        memberKeys.forEach(k -> k.write(output));
    }

    @Override
    public String toTypeExpression() {
        return NncUtils.join(memberKeys, TypeKey::toTypeExpression, "|");
    }

    @Override
    public UnionType toType(TypeDefProvider typeDefProvider) {
        return new UnionType(NncUtils.mapUnique(memberKeys, k -> k.toType(typeDefProvider)));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitUnionTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.UNION;
    }
}
