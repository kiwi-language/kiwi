package tech.metavm.object.meta.rest.dto;

import tech.metavm.dto.RefDTO;

import java.util.List;

public record TypeVariableParam(
        RefDTO genericDeclarationRef,
        int index,
        List<RefDTO> boundRefs
) implements TypeParam {
    @Override
    public int getType() {
        return 5;
    }

    @Override
    public TypeKey getTypeKey() {
        return new TypeVariableKey(genericDeclarationRef, index);
    }
}
