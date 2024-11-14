package org.metavm.object.type.rest.dto;

import java.util.List;

public record TypeVariableDTO(
        String id,
        String name,
        String genericDeclarationId,
        int index,
        List<String> bounds
) implements TypeDefDTO {

    @Override
    public int getDefKind() {
        return 2;
    }
}
