package tech.metavm.object.instance.core;

import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;

import javax.annotation.Nullable;

public record InstanceContextConfig(
        long appId,
        boolean readonly,
        @Nullable IInstanceContext parent,
        IndexSource indexSource,
        TypeProvider typeProvider,
        MappingProvider mappingProvider,
        ParameterizedFlowProvider parameterizedFlowProvider
) {
}
