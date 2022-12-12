package tech.metavm.object.meta.rest.dto;

public record ArrayTypeParamDTO (
        Long elementTypeId,
        TypeDTO elementType
) {
}
