package tech.metavm.object.instance.rest;

import java.util.List;

public record InstanceFieldDTO(
        long fieldId,
        String fieldName,
        Integer type,
        Boolean multiValued,
        Object value,
        String displayValue,
        List<ValueDTO> values
) {

    public static InstanceFieldDTO valueOf(long fieldId, Object value) {
        return new InstanceFieldDTO(
                fieldId,
                null,
                null,
                null,
                value,
                null,
                null
        );
    }

}
