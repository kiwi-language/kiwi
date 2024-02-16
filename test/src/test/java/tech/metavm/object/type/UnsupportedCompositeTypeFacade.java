package tech.metavm.object.type;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UnsupportedCompositeTypeFacade implements CompositeTypeFacade {

    @Override
    public ArrayType getArrayType(Type elementType, ArrayKind kind, @Nullable Long tmpId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FunctionType getFunctionType(List<Type> parameterTypes, Type returnType, @Nullable Long tmpId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UnionType getUnionType(Set<Type> types, @Nullable Long tmpId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IntersectionType getIntersectionType(Set<Type> types, @Nullable Long tmpId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public UncertainType getUncertainType(Type lowerBound, Type upperBound, @Nullable Long tmpId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ClassType getParameterizedType(ClassType template, List<Type> typeArguments, ResolutionStage resolutionStage, DTOProvider dtoProvider) {
        throw new UnsupportedOperationException();
    }
}
