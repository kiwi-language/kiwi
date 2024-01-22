package tech.metavm.object.instance.rest;

import java.io.Serializable;
import java.util.Set;

public record InstanceFieldDTO(
        Long fieldId,
        String fieldName,
        Integer type,
        Boolean multiValued,
        FieldValue value
) implements Serializable {

    public static InstanceFieldDTO create(Long fieldId, FieldValue value) {
        return new InstanceFieldDTO(
                fieldId,
                null,
                null,
                null,
                value
        );
    }

    public boolean valueEquals(InstanceFieldDTO that, Set<String> newIds) {
        return fieldId.equals(that.fieldId) && value.valueEquals(that.value, newIds);
    }

}
