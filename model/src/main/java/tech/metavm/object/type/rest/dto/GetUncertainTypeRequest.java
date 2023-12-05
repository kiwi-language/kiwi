package tech.metavm.object.type.rest.dto;

public record GetUncertainTypeRequest(
        Long lowerBoundId,
        Long upperBoundId
) {

}
