package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.FieldValue;

import javax.annotation.Nullable;
import java.util.List;

public record FlowExecutionRequest(
        long flowId,
        @Nullable String instanceId,
        List<FieldValue> arguments
) {
}
