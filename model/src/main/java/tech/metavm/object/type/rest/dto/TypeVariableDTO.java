package tech.metavm.object.type.rest.dto;

import java.util.List;

public record TypeVariableDTO(
        String id,
        String name,
        String code,
        String genericDeclarationId,
        int index,
        List<String> bounds
) implements TypeDefDTO {

}
