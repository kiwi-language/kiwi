package org.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.jsonk.Json;
import org.metavm.object.instance.core.Id;
import org.metavm.util.MvOutput;
import org.metavm.util.WireTypes;

@Json
public record ClassTypeKey(@NotNull Id id) implements TypeKey, GenericDeclarationRefKey {
    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.CLASS_TYPE);
        output.writeId(id);
    }

    @Override
    public String toTypeExpression() {
        return String.format("$$%s", id);
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitClassTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.CLASS_TYPE;
    }

}
