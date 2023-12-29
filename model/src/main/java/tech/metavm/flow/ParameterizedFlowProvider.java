package tech.metavm.flow;

import tech.metavm.object.type.Type;

import java.util.List;

public interface ParameterizedFlowProvider {

    <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments);

    <T extends Flow> T getExistingFlow(T template, List<? extends Type> typeArguments);

}
