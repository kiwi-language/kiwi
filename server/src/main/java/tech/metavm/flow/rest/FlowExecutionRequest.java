package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.FieldValue;

import java.util.List;

public record FlowExecutionRequest(
        long flowId,
        String instanceId,
        List<FieldValue> arguments
) {
}
