package tech.metavm.object.view.rest.dto;

import tech.metavm.object.instance.rest.FieldValue;

public record InstanceFormFieldDTO(
        long fieldId,
        FieldValue value
) {
}
