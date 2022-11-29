package tech.metavm.object.instance.rest;

public record InstanceFieldDTO(
        Long fieldId,
        String fieldName,
        Integer type,
        Boolean multiValued,
        Object value,
        String displayValue
) {

    public static InstanceFieldDTO valueOf(Long fieldId, Object value) {
        return new InstanceFieldDTO(
                fieldId,
                null,
                null,
                null,
                value,
                null
        );
    }

}
