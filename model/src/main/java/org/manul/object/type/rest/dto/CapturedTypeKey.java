package org.manul.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import org.jsonk.Json;
import org.manul.object.instance.core.Id;
import org.manul.util.MvOutput;
import org.manul.util.WireTypes;

@Json
public record CapturedTypeKey(@NotNull Id variableId) implements TypeKey {
    @Override
    public void write(MvOutput output) {
        output.writeId(variableId);
    }

    @Override
    public String toTypeExpression() {
        return String.format("#$$%s", variableId);
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitCapturedTypeKey(this);
    }

    @Override
    public int getCode() {
        return WireTypes.CAPTURED_TYPE;
    }
}
