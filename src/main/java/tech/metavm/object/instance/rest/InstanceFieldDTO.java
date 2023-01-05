package tech.metavm.object.instance.rest;

public record InstanceFieldDTO(
        Long fieldId,
        String fieldName,
        Integer type,
        Boolean multiValued,
        FieldValueDTO value
) {

    public static InstanceFieldDTO valueOf(Long fieldId, FieldValueDTO value) {
        return new InstanceFieldDTO(
                fieldId,
                null,
                null,
                null,
                value
        );
    }

}
