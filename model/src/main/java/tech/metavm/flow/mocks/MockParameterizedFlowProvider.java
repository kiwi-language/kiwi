package tech.metavm.flow.mocks;

import tech.metavm.flow.Flow;
import tech.metavm.flow.Method;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.object.type.ResolutionStage;
import tech.metavm.object.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockParameterizedFlowProvider implements ParameterizedFlowProvider {

    private final Map<Key, Flow> map = new HashMap<>();
    private final TypeProviders typeProviders;

    public MockParameterizedFlowProvider(TypeProviders typeProviders) {
        this.typeProviders = typeProviders;
    }

    @Override
    public <T extends Flow> T getParameterizedFlow(T template, List<? extends Type> typeArguments) {
        //noinspection unchecked
        return (T) map.computeIfAbsent(new Key(template, typeArguments),
                k -> createParameterizedFlow(template, typeArguments));
    }

    private Flow createParameterizedFlow(Flow template, List<? extends Type> typeArguments) {
        var subst = typeProviders.createSubstitutor(template, template.getTypeParameters(), typeArguments, ResolutionStage.DEFINITION);
        if (template instanceof Method method)
            subst.enterElement(method.getDeclaringType());
        subst.enterElement(template);
        return (Flow) template.accept(subst);
    }

    @Override
    public <T extends Flow> T getExistingFlow(T template, List<? extends Type> typeArguments) {
        //noinspection unchecked
        return (T) map.get(new Key(template, typeArguments));
    }

    private record Key(Flow template, List<? extends Type> typeArguments) {
    }

}
