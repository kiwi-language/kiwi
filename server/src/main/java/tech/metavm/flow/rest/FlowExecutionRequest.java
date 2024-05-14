package tech.metavm.flow.rest;

import tech.metavm.object.instance.rest.FieldValue;

import javax.annotation.Nullable;
import java.util.List;

public record FlowExecutionRequest(
        FlowRefDTO flowRef,
        @Nullable String instanceId,
        List<FieldValue> arguments
) {

}
