package tech.metavm.entity.natives;

import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.InstanceRepository;

public record NativeCallContext(
        InstanceRepository instanceRepository,
        ParameterizedFlowProvider parameterizedFlowProvider
) {

}
