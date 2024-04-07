package tech.metavm.entity.natives;

import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.CapturedType;
import tech.metavm.object.type.CompositeTypeFacade;
import tech.metavm.object.type.Type;

import java.util.Map;

public record DefaultCallContext(
        InstanceRepository instanceRepository,
        ParameterizedFlowProvider parameterizedFlowProvider,
        CompositeTypeFacade compositeTypeFacade
) implements CallContext {
}
