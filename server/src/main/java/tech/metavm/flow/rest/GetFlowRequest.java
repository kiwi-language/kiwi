package tech.metavm.flow.rest;

public record GetFlowRequest(
        String id,
        boolean includeNodes
) {
}
