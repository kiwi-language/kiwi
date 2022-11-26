package tech.metavm.object.instance.rest;

public record InstanceFieldDTO(
        long fieldId,
        String fieldName,
        Integer type,
        Boolean multiValued,
        Object value,
        String displayValue
) {

    public static InstanceFieldDTO valueOf(long fieldId, Object value) {
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
