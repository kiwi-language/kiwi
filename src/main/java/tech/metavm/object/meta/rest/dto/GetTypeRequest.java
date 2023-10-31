package tech.metavm.object.meta.rest.dto;

public record GetTypeRequest(
        long id,
        boolean includingPropertyTypes
) {
}
