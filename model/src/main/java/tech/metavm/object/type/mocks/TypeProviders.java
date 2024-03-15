package tech.metavm.object.type.mocks;

import tech.metavm.entity.EntityRepository;
import tech.metavm.entity.MemTypeRegistry;
import tech.metavm.entity.TypeRegistry;
import tech.metavm.entity.mocks.MockEntityRepository;
import tech.metavm.flow.mocks.MockParameterizedFlowProvider;
import tech.metavm.object.type.*;
import tech.metavm.object.type.generic.SubstitutorV2;

import java.util.List;

public class TypeProviders {

    public final MemTypeRegistry typeRegistry = new MemTypeRegistry();
    public final MockFunctionTypeProvider functionTypeProvider = new MockFunctionTypeProvider();
    public final MockUncertainTypeProvider uncertainTypeProvider = new MockUncertainTypeProvider();
    public final MockUnionTypeProvider unionTypeProvider = new MockUnionTypeProvider();
    public final MockIntersectionTypeProvider intersectionTypeProvider = new MockIntersectionTypeProvider();
    public final MockArrayTypeProvider arrayTypeProvider = new MockArrayTypeProvider();
    public final MockParameterizedTypeProvider parameterizedTypeProvider = new MockParameterizedTypeProvider(this);
    public final MockParameterizedFlowProvider parameterizedFlowProvider = new MockParameterizedFlowProvider(this);
    public final EntityRepository entityRepository = new MockEntityRepository(typeRegistry);
    public final MockTypeRepository typeRepository = new MockTypeRepository();

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

    public void addType(Type type) {
        entityRepository.bind(type);
        typeRepository.save(type);
        switch (type) {
            case ArrayType arrayType -> arrayTypeProvider.add(arrayType);
            case ClassType classType when !classType.getTypeArguments().isEmpty() ->
                parameterizedTypeProvider.add(classType);
            case UnionType unionType -> unionTypeProvider.add(unionType);
            case IntersectionType intersectionType -> intersectionTypeProvider.add(intersectionType);
            case FunctionType functionType -> functionTypeProvider.add(functionType);
            case UncertainType uncertainType -> uncertainTypeProvider.add(uncertainType);
            default -> {}
        }
    }

}
