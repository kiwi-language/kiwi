package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.FieldValueDTO;

import java.util.List;

public record FlowExecutionRequest(
        long flowId,
        long instanceId,
        List<FieldValueDTO> arguments
) {
}
