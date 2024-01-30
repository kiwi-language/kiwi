package tech.metavm.object.type;

import tech.metavm.flow.Flow;
import tech.metavm.flow.ParameterizedFlowProvider;

import java.util.List;

public class UnsupportedParameterizedFlowProvider implements ParameterizedFlowProvider {
    @Override
    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Flow> T getExistingFlow(T template, List<? extends Type> typeArguments) {
        throw new UnsupportedOperationException();
    }
}
