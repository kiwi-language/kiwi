package tech.metavm.object.type.mocks;

import tech.metavm.entity.EntityRepository;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.flow.mocks.MockParameterizedFlowProvider;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.SubstitutorV2;

import java.util.List;

public class TypeProviders {

    public final FunctionTypeProvider functionTypeProvider = new MockFunctionTypeProvider();
    public final UncertainTypeProvider uncertainTypeProvider = new MockUncertainTypeProvider();
    public final UnionTypeProvider unionTypeProvider = new MockUnionTypeProvider();
    public final IntersectionTypeProvider intersectionTypeProvider = new MockIntersectionTypeProvider();
    public final ArrayTypeProvider arrayTypeProvider = new MockArrayTypeProvider();
    public final ParameterizedTypeProvider parameterizedTypeProvider = new MockParameterizedTypeProvider(this);
    public final ParameterizedFlowProvider parameterizedFlowProvider = new MockParameterizedFlowProvider(this);
    private final EntityRepository entityRepository = new MockEntityRepository();

    public CompositeTypeFacade createFacade() {
        return new CompositeTypeFacadeImpl(
                arrayTypeProvider,
                functionTypeProvider,
                unionTypeProvider,
                intersectionTypeProvider,
                uncertainTypeProvider,
                parameterizedTypeProvider
        );
    }

    public SubstitutorV2 createSubstitutor(Object template, List<TypeVariable> typeParameters, List<? extends Type> typeArguments, ResolutionStage stage) {
        return new SubstitutorV2(
                template,
                typeParameters,
                typeArguments,
                stage,
                entityRepository,
                createFacade(),
                parameterizedTypeProvider,
                parameterizedFlowProvider,
                new MockDTOProvider()
        );
    }
}
