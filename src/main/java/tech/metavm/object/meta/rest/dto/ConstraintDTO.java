package tech.metavm.object.meta.rest.dto;

public record ConstraintDTO(
        Long id,
        int kind,
        long typeId,
        Object param
) {

    public <T> T getParam() {
        return (T) param;
    }

}
