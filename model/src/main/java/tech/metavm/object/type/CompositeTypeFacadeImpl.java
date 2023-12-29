package tech.metavm.object.type;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CompositeTypeFacadeImpl implements CompositeTypeFacade {

    private final ArrayTypeProvider arrayTypeProvider;
    private final FunctionTypeProvider functionTypeProvider;
    private final UnionTypeProvider unionTypeProvider;
    private final IntersectionTypeProvider intersectionTypeProvider;
    private final UncertainTypeProvider uncertainTypeProvider;
    private final ParameterizedTypeProvider parameterizedTypeProvider;

    public CompositeTypeFacadeImpl(ArrayTypeProvider arrayTypeProvider, FunctionTypeProvider functionTypeProvider, UnionTypeProvider unionTypeProvider, IntersectionTypeProvider intersectionTypeProvider, UncertainTypeProvider uncertainTypeProvider, ParameterizedTypeProvider parameterizedTypeProvider) {
        this.arrayTypeProvider = arrayTypeProvider;
        this.functionTypeProvider = functionTypeProvider;
        this.unionTypeProvider = unionTypeProvider;
        this.intersectionTypeProvider = intersectionTypeProvider;
        this.uncertainTypeProvider = uncertainTypeProvider;
        this.parameterizedTypeProvider = parameterizedTypeProvider;
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
    public Type getParameterizedType(ClassType template, List<Type> typeArguments, ResolutionStage resolutionStage, DTOProvider dtoProvider) {
        return parameterizedTypeProvider.getParameterizedType(template, typeArguments, resolutionStage, dtoProvider);
    }

}
