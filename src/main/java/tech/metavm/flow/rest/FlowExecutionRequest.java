package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.InstanceFieldDTO;

import java.util.List;

public record FlowExecutionRequest(
        long flowId,
        long instanceId,
        List<InstanceFieldDTO> fields
) {
}
