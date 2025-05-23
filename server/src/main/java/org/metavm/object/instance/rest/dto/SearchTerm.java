package org.metavm.object.instance.rest.dto;

public record SearchTerm(
        String field,
        ValueDTO value,
        ValueDTO min,
        ValueDTO max
) {


    public static SearchTerm of(String field, ValueDTO value) {
        return new SearchTerm(field, value, null, null);
    }

    public static SearchTerm ofRange(String field, ValueDTO min, ValueDTO max) {
        return new SearchTerm(field, null, min, max);
    }

}
