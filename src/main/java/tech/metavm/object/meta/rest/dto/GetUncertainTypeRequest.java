package tech.metavm.object.meta.rest.dto;

public record GetUncertainTypeRequest(
        Long lowerBoundId,
        Long upperBoundId
) {

}
