package org.metavm.flow.rest;

import java.util.List;

public record GetParameterizedFlowRequest(
        String templateId,
        List<String> typeArgumentIds
) {
}
