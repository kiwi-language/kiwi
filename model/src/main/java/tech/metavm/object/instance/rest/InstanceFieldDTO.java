package tech.metavm.object.instance.rest;

import java.io.Serializable;

public record InstanceFieldDTO(
        Long fieldId,
        String fieldName,
        Integer type,
        Boolean multiValued,
        FieldValue value
) implements Serializable {

    public static InstanceFieldDTO valueOf(Long fieldId, FieldValue value) {
        return new InstanceFieldDTO(
                fieldId,
                null,
                null,
                null,
                value
        );
    }

}
