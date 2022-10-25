package tech.metavm.object.meta.rest.dto;

import javax.annotation.Nullable;
import java.util.List;

public record ColumnDTO(
        Long id,
        String name,
        int type,
        int access,
        long ownerId,
        @Nullable Long targetId,
        String targetName,
        boolean required,
        boolean multiValued,
        boolean unique,
        boolean asTitle,
        Object defaultValue,
        List<ChoiceOptionDTO> choiceOptions
) {
}
