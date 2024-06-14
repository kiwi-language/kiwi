package org.metavm.object.instance.rest;

public record ConditionDTO (
        long fieldId,
        int compareType,
        Object value
) {

}
