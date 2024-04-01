package tech.metavm.entity.natives;

import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.CompositeTypeFacade;

public record DefaultCallContext(
        InstanceRepository instanceRepository,
        ParameterizedFlowProvider parameterizedFlowProvider,
        CompositeTypeFacade compositeTypeFacade
) implements CallContext {
}
