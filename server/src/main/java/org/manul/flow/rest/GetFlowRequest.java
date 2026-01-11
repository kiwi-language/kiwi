package org.manul.flow.rest;

public record GetFlowRequest(
        String id,
        boolean includeNodes
) {
}
