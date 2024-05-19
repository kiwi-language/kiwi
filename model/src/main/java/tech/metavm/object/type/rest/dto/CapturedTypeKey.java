package tech.metavm.object.type.rest.dto;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.CapturedType;
import tech.metavm.object.type.CapturedTypeVariable;
import tech.metavm.object.type.TypeDefProvider;
import tech.metavm.util.InstanceOutput;

public record CapturedTypeKey(@NotNull Id variableId) implements TypeKey {
    @Override
    public void write(InstanceOutput output) {
        output.writeId(variableId);
    }

    @Override
    public String toTypeExpression() {
        return String.format("#$$%s", variableId);
    }

    @Override
    public CapturedType toType(TypeDefProvider typeDefProvider) {
        return new CapturedType((CapturedTypeVariable) typeDefProvider.getTypeDef(variableId));
    }

    @Override
    public <R> R accept(TypeKeyVisitor<R> visitor) {
        return visitor.visitCapturedTypeKey(this);
    }

    @Override
    public int getCode() {
        return TypeKeyCodes.CAPTURED;
    }
}
