package org.metavm.object.view.rest.dto;

import org.metavm.object.instance.rest.FieldValue;

public record InstanceFormFieldDTO(
        long fieldId,
        FieldValue value
) {
}
