package tech.metavm.object.type;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public interface CompositeTypeFacade extends FunctionTypeProvider, ArrayTypeProvider, UnionTypeProvider, IntersectionTypeProvider, UncertainTypeProvider {

    @Override
    ArrayType getArrayType(Type elementType, ArrayKind kind, @Nullable Long tmpId);

    @Override
    FunctionType getFunctionType(List<Type> parameterTypes, Type returnType, @Nullable Long tmpId);

    @Override
    UnionType getUnionType(Set<Type> types, @Nullable Long tmpId);

    @Override
    IntersectionType getIntersectionType(Set<Type> types, @Nullable Long tmpId);

    @Override
    UncertainType getUncertainType(Type lowerBound, Type upperBound, @Nullable Long tmpId);

    ClassType getParameterizedType(ClassType template, List<Type> typeArguments,
                              ResolutionStage resolutionStage, DTOProvider dtoProvider);

    default ClassType getParameterizedType(ClassType template, List<Type> typeArguments) {
        return getParameterizedType(template, typeArguments, ResolutionStage.DEFINITION, new MockDTOProvider());
    }

}
