package org.metavm.object.type.rest.dto;

public record GetUncertainTypeRequest(
        String lowerBoundId,
        String upperBoundId
) {

}
