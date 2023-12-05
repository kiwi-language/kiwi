package tech.metavm.flow.rest;

public record GetFlowRequest(
        long id,
        boolean includeNodes
) {
}
