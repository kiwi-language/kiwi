package tech.metavm.object.type;

import tech.metavm.entity.IEntityContext;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class CompositeTypeFacadeImpl implements CompositeTypeFacade {

    public static CompositeTypeFacade fromContext(IEntityContext context) {
        return new CompositeTypeFacadeImpl(
                new ContextArrayTypeProvider(context),
                context.getFunctionTypeContext(),
                context.getUnionTypeContext(),
                context.getIntersectionTypeContext(),
                context.getUncertainTypeContext(),
                context.getGenericContext()
        );
    }

    private final ArrayTypeProvider arrayTypeProvider;
    private final FunctionTypeProvider functionTypeProvider;
    private final UnionTypeProvider unionTypeProvider;
    private final IntersectionTypeProvider intersectionTypeProvider;
    private final UncertainTypeProvider uncertainTypeProvider;
    private final ParameterizedTypeRepository parameterizedTypeRepository;

    public CompositeTypeFacadeImpl(ArrayTypeProvider arrayTypeProvider, FunctionTypeProvider functionTypeProvider, UnionTypeProvider unionTypeProvider, IntersectionTypeProvider intersectionTypeProvider, UncertainTypeProvider uncertainTypeProvider, ParameterizedTypeRepository parameterizedTypeRepository) {
        this.arrayTypeProvider = arrayTypeProvider;
        this.functionTypeProvider = functionTypeProvider;
        this.unionTypeProvider = unionTypeProvider;
        this.intersectionTypeProvider = intersectionTypeProvider;
        this.uncertainTypeProvider = uncertainTypeProvider;
        this.parameterizedTypeRepository = parameterizedTypeRepository;
    }

    @Override
    public ArrayType getArrayType(Type elementType, ArrayKind kind, @Nullable Long tmpId) {
        return arrayTypeProvider.getArrayType(elementType, kind);
    }

    @Override
    public FunctionType getFunctionType(List<Type> parameterTypes, Type returnType, @org.jetbrains.annotations.Nullable Long tmpId) {
        return functionTypeProvider.getFunctionType(parameterTypes, returnType, tmpId);
    }

    @Override
    public UnionType getUnionType(Set<Type> types, @org.jetbrains.annotations.Nullable Long tmpId) {
        return unionTypeProvider.getUnionType(types, tmpId);
    }

    @Override
    public IntersectionType getIntersectionType(Set<Type> types, @org.jetbrains.annotations.Nullable Long tmpId) {
        return intersectionTypeProvider.getIntersectionType(types, tmpId);
    }

    @Override
    public UncertainType getUncertainType(Type lowerBound, Type upperBound, @org.jetbrains.annotations.Nullable Long tmpId) {
        return uncertainTypeProvider.getUncertainType(lowerBound, upperBound, tmpId);
    }

    @Override
    public ClassType getParameterizedType(ClassType template, List<? extends Type> typeArguments, ResolutionStage resolutionStage, DTOProvider dtoProvider) {
        return parameterizedTypeRepository.getParameterizedType(template, typeArguments, resolutionStage, dtoProvider);
    }

}
