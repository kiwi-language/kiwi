package org.metavm.object.type.rest.dto;

import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.type.UnionType;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

import java.util.Set;

public record UnionTypeKey(Set<TypeKey> memberKeys) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.UNION_TYPE);
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
        return WireTypes.UNION_TYPE;
    }
}
