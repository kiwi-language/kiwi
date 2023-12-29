package tech.metavm.object.type.rest.dto;

import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.Access;

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
        FieldValue defaultValue,
        List<ChoiceOptionDTO> choiceOptions
) {

    public static ColumnDTO createPrimitive(String name,
                                            int type,
                                            boolean required,
                                            boolean unique) {
        return new ColumnDTO(
                null, name, type, Access.PUBLIC.code(), null, null,
                null, required, false, unique,
                null, null
        );
    }

}
