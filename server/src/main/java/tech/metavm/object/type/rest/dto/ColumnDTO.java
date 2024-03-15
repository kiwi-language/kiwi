package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.object.instance.rest.FieldValue;
import tech.metavm.object.type.Access;

import javax.annotation.Nullable;
import java.util.List;

public record ColumnDTO(
        String id,
        String name,
        int type,
        int access,
        String ownerId,
        @Nullable String targetId,
        String targetName,
        boolean required,
        boolean multiValued,
        boolean unique,
        FieldValue defaultValue,
        List<ChoiceOptionDTO> choiceOptions
) implements BaseDTO  {

    public static ColumnDTO createPrimitive(
            Long tmpId, String name,
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
