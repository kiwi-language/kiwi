package tech.metavm.entity.natives;

import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.CapturedType;
import tech.metavm.object.type.CompositeTypeFacade;
import tech.metavm.object.type.Type;

import java.util.Map;

public interface CallContext {

    InstanceRepository instanceRepository();

    ParameterizedFlowProvider parameterizedFlowProvider();

    CompositeTypeFacade compositeTypeFacade();

    default Map<CapturedType, Type> capturedTypes() {
        return Map.of();
    }

}
