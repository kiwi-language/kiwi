package tech.metavm.object.meta.rest.dto;

import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.meta.Access;

import javax.annotation.Nullable;
import java.util.List;

public record ColumnDTO(
        Long id,
        String name,
        int type,
        int access,
        Long ownerId,
        @Nullable Long targetId,
        String targetName,
        boolean required,
        boolean multiValued,
        boolean unique,
        boolean asTitle,
        FieldValue defaultValue,
        List<ChoiceOptionDTO> choiceOptions
) {

    public static ColumnDTO createPrimitive(String name,
                                            int type,
                                            boolean required,
                                            boolean unique,
                                            boolean asTitle) {
        return new ColumnDTO(
                null, name, type, Access.PUBLIC.code(), null, null,
                null, required, false, unique, asTitle,
                null, null
        );
    }

}
