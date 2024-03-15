package tech.metavm.object.type.rest.dto;

import java.util.List;

public record TypeVariableParam(
        String genericDeclarationId,
        int index,
        List<String> boundIds
) implements TypeParam {
    @Override
    public int getType() {
        return 5;
    }

    @Override
    public TypeKey getTypeKey() {
        return new TypeVariableKey(genericDeclarationId, index);
    }
}
