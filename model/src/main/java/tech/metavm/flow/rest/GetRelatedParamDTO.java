package tech.metavm.flow.rest;

public record GetRelatedParamDTO (
        ValueDTO objectId,
        long fieldId
) {
}
