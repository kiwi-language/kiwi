package tech.metavm.object.meta.rest.dto;

import java.util.List;

public record FieldDTO(
        Long id,
        String name,
        int type,
        int access,
        boolean required,
        Object defaultValue,
        boolean unique,
        boolean asTitle,
        boolean multiValued,
        long ownerId,
        Long targetId,
        String targetName,
        List<ChoiceOptionDTO> choiceOptions,
        Long typeId
) {

    public boolean nullable() {
        return !required;
    }

}
