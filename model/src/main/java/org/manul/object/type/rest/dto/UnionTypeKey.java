package org.manul.object.type.rest.dto;

import org.jsonk.Json;
import org.manul.util.MvOutput;
import org.manul.util.Utils;
import org.manul.util.WireTypes;

import java.util.Set;

@Json
public record UnionTypeKey(Set<TypeKey> memberKeys) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.UNION_TYPE);
        output.writeInt(memberKeys.size());
        memberKeys.forEach(k -> k.write(output));
    }

    @Override
    public String toTypeExpression() {
        return Utils.join(memberKeys, TypeKey::toTypeExpression, "|");
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
