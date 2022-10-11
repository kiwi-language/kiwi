package tech.metavm.flow.rest;

import java.util.List;

public record FlowExecutionRequest(
        long flowId,
        long instanceId,
        List<FieldValueDTO> fields
) {
}
